package org.callie.model

import scala.collection.mutable.ListBuffer
import com.jogamp.opengl.GL4
import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.util.texture.TextureIO
import org.callie.jogl.{buffers, Gl, GlEventListener}
import org.callie.math.Vector3
import buffers._

object Graphics{
  var parts:Array[GlObject] = Array()

  def init(gl:GL4){
    for(o <- parts)o.init(gl)
  }

  def display(gl:GL4){
    for(o <- parts)o.display(gl)
  }

}

trait GlObject{

  def init(implicit gl:GL4)
  
  def display(implicit gl:GL4)
    
}

class ObjectGroup(objs:GlObject*) extends GlObject{
  
  override def init(implicit gl:GL4){
    for(o <- objs) o.init
  }
  
  override def display(implicit gl:GL4){
    for(o <- objs) o.display
  }
  
}

class TextureGroup(ev: GlEventListener, image:String, objs:GlObject*) extends ObjectGroup(objs:_*){
  
  var texId : Int = _
    
  override def init(implicit gl:GL4){
    val texture = TextureIO.newTextureData(gl.getGLProfile, getClass.getResourceAsStream(image), false, TextureIO.PNG)
    
    texId = ev.createTexture{
	  gl.glPixelStorei(Gl.TEXTURE_2D, texture.getAlignment)
	  gl.glTexImage2D(Gl.TEXTURE_2D, 0, texture.getInternalFormat, texture.getWidth, texture.getHeight, texture.getBorder, texture.getPixelFormat, texture.getPixelType, texture.getBuffer)
	
	  gl.glGenerateMipmap(Gl.TEXTURE_2D)
	
	  gl.glTexParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_WRAP_S, Gl.REPEAT)
	  gl.glTexParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_WRAP_T, Gl.REPEAT)
	  gl.glTexParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_MAG_FILTER, Gl.LINEAR)
	  gl.glTexParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_MIN_FILTER, Gl.LINEAR_MIPMAP_LINEAR)
    
      super.init
    }
    
    texture.destroy()    
  }
  
  override def display(implicit gl:GL4){
    ev.bindTexture(texId){    
      super.display
    }
  }
  
}

class StaticObject(ev:GlEventListener, m:Mod) extends GlObject{
  
  val (coords, indices) = {
    // stack
    val s = ListBuffer[Mod.vtn]()

    val a = ListBuffer[Float]()
    val i = ListBuffer[Int]()

    def vtx(sel: Mod.vtn){
      val ind = s.indexOf(sel)
      if(ind == -1){
        val v = m.points(sel._1)
        a += v._1 += v._2 += v._3

        val t = m.uvCoord(sel._2)
        a += t._1 += t._2

        val n = m.normals(sel._3)
        a += n._1 += n._2 += n._3

        i += s.size
        s += sel
      }else i += ind
    }

    for(f <- m.faces){
      vtx(f._1)
      vtx(f._2)
      vtx(f._3)
    }

    (a.toArray, i.toArray)
  }
    
  var vao : Int = _
  var vbi : Int = _
  
  override def init(implicit gl:GL4){
    vao = ev.createVertexArray{
      gl.glEnableVertexAttribArray(0)
      gl.glEnableVertexAttribArray(1)
      gl.glEnableVertexAttribArray(2)
      
      ev.createBuffer(Gl.ARRAY_BUFFER){
        coords.asBuffer(gl.glBufferData(Gl.ARRAY_BUFFER, _, _, Gl.STATIC_DRAW))

        gl.glVertexAttribPointer(0, 3, Gl.FLOAT, false, (3+2+3)*Buffers.SIZEOF_FLOAT, 0*Buffers.SIZEOF_FLOAT)
        gl.glVertexAttribPointer(1, 2, Gl.FLOAT, false, (3+2+3)*Buffers.SIZEOF_FLOAT, 3*Buffers.SIZEOF_FLOAT)
        gl.glVertexAttribPointer(2, 3, Gl.FLOAT, false, (3+2+3)*Buffers.SIZEOF_FLOAT, 5*Buffers.SIZEOF_FLOAT)
      }
    }
    
    vbi = ev.createBuffer(Gl.ELEMENT_ARRAY_BUFFER){
      indices.asBuffer(gl.glBufferData(Gl.ELEMENT_ARRAY_BUFFER, _, _, Gl.STATIC_DRAW))
    }
  }
  
  override def display(implicit gl:GL4){
    ev.bindVertexArray(vao){
      ev.bindBuffer(Gl.ELEMENT_ARRAY_BUFFER, vbi){
        gl.glDrawElements(Gl.TRIANGLES, indices.length, Gl.UNSIGNED_INT, 0)
      }
    }
  }
  
}

class MorfingObject(ev:GlEventListener, m:Mod) extends GlObject{
  val (coords, indices, projPoint, projNormals) = {
    // stack
    val s = ListBuffer[Mod.vtn]()

    val a = ListBuffer[Float]()
    val i = ListBuffer[Int]()
    
    val prPoint = for(p <- m.points) yield ( Vector3(p._1, p._2, p._3), ListBuffer[Int]() )
    val prNormals = for(p <- m.normals) yield ( Vector3(p._1, p._2, p._3), ListBuffer[Int]() )
    
    for(p <- m.points) yield ( Vector3(p._1, p._2, p._3), ListBuffer[Int]() )
        
    def vtx(sel: Mod.vtn){
      val ind = s.indexOf(sel)
      if(ind == -1){
        val v = m.points(sel._1)
        prPoint(sel._1)._2 += a.size
        a += v._1 += v._2 += v._3
        
        val t = m.uvCoord(sel._2)
        a += t._1 += t._2

        val n = m.normals(sel._3)
        prNormals(sel._3)._2 += a.size
        a += n._1 += n._2 += n._3

        i += s.size
        s += sel
      }else i += ind
    }

    for(f <- m.faces){
      vtx(f._1)
      vtx(f._2)
      vtx(f._3)
    }

    val c = a.toArray
    
    (c, i.toArray, 
      prPoint.map{it => (it._1, Vector3(c, it._2.toArray))}.toArray, 
      prNormals.map{it => (it._1, Vector3(c, it._2.toArray))}.toArray
    )
  }
  
  var vao : Int = _
  var vbi : Int = _
  var vbo : Int = _
  
  override def init(implicit gl:GL4){
    vao = ev.createVertexArray{
      gl.glEnableVertexAttribArray(0)
      gl.glEnableVertexAttribArray(1)
      gl.glEnableVertexAttribArray(2)
      
      vbo = ev.createBuffer(Gl.ARRAY_BUFFER){
        coords.asBuffer(gl.glBufferData(Gl.ARRAY_BUFFER, _, _, Gl.DYNAMIC_DRAW))

        gl.glVertexAttribPointer(0, 3, Gl.FLOAT, false, (3+2+3)*Buffers.SIZEOF_FLOAT, 0*Buffers.SIZEOF_FLOAT)
        gl.glVertexAttribPointer(1, 2, Gl.FLOAT, false, (3+2+3)*Buffers.SIZEOF_FLOAT, 3*Buffers.SIZEOF_FLOAT)
        gl.glVertexAttribPointer(2, 3, Gl.FLOAT, false, (3+2+3)*Buffers.SIZEOF_FLOAT, 5*Buffers.SIZEOF_FLOAT)
      }
    }
    
    vbi = ev.createBuffer(Gl.ELEMENT_ARRAY_BUFFER){
      indices.asBuffer(gl.glBufferData(Gl.ELEMENT_ARRAY_BUFFER, _, _, Gl.DYNAMIC_DRAW))
    }
  }
  
  override def display(implicit gl:GL4){
    ev.bindVertexArray(vao){
      
      ev.bindBuffer(Gl.ARRAY_BUFFER, vbo){
        coords.asBuffer(gl.glBufferData(Gl.ARRAY_BUFFER, _, _, Gl.DYNAMIC_DRAW))
      }
      
      ev.bindBuffer(Gl.ELEMENT_ARRAY_BUFFER, vbi){
        gl.glDrawElements(Gl.TRIANGLES, indices.length, Gl.UNSIGNED_INT, 0)
      }     
    }
  }
}


