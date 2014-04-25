package org.callie.ringing

import scala.util.parsing.combinator.RegexParsers
import org.callie.math.{Matrix4, Vector3}
import org.callie.math.intr.Accl
import org.callie.jogl.GL4EventListener
import org.callie.model.MorfingObject
import org.callie.Mod

abstract class Node(ind:List[Int]){
  
  def apply(ev:GL4EventListener, m:Mod) = {
    val o = new MorfingObject(ev, m)
    (o, join(ind.map(o.projPoint(_)).toArray,
        ind.map(o.projNormals(_)).toArray) )
  }
  
  type Mapping = Array[(Vector3, Vector3)]
  
  def join(coord : Mapping, normals: Mapping, parent:Option[IntrTravJoint] = None) : Joint
}

class IntNode(name:String, v:Vector3, ind:List[Int], childs:List[Node]) extends Node(ind){
  override def join(coord : Mapping, normals: Mapping, parent:Option[IntrTravJoint]) = {
    val ax = new Accl; val ay = new Accl; val az = new Accl
    val m = Matrix4(v)
    
    if(childs.isEmpty) new IntrJoint(name, m, ax, ay, az, coord, normals)
    else{
      val ch = new Array[Joint](childs.size)
      val j = new IntrTravJoint(name, m, ax, ay, az, ch, coord, normals)
      val sj = Some(j)
      //childs.zipWithIndex.foreach{ i => ch(i._2) = i._1.join(coord, normals, sj) }
      for(i <- 0 until ch.length) ch(i) = childs(i).join(coord, normals, sj)
      j
    }
  }
}

class LinNode(name:String,
              ix:LinearJoint.Parms, iy:LinearJoint.Parms, iz:LinearJoint.Parms, ind:List[Int])  extends Node(ind){
  override def join(coord : Mapping, normals: Mapping, parent:Option[IntrTravJoint]) = new LinearJoint(name, parent.get, ix, iy, iz, coord, normals)
}

object Node extends RegexParsers {
 
  def index: Parser[Int] = """\d+""".r ^^ (_.toInt)

  def float: Parser[Float] = """[+-]?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?""".r ^^ (_.toFloat)
  
  def name: Parser[String] = "[a-zA-Z0-9_]+".r // ^^ (_.toInt)
  
  def normal : Parser[(Float, Float, Float)] = "normal" ~> ":" ~> repsep(float, ",") ^^ { a =>
    assert(a.size == 3)
    (a(0), a(1), a(2)) 
  }
  
  type LinMapTp = (String, String, Float)
  
  def linMap : Parser[LinMapTp] = ( ("x" | "y" | "z") <~ "=" ) ~ ( ("x" | "y" | "z") <~ ":" ) ~ float ^^ { a => (a._1._1, a._1._2, a._2) }
  
  def linear : Parser[List[LinMapTp]] = "linear" ~> ":" ~> repsep(linMap, ",")
    
  def node : Parser[Node] = name ~ ("(" ~> ( normal | linear ) <~ ")" ) ~ ("[" ~> repsep(index, ",") <~ "]" ) ~ ( "{" ~> rep(node) <~ "}" ).? ^^ { case nm ~ tp ~ pt ~ ch =>    
    tp match{
      case (x:Float, y:Float, z:Float) =>
        new IntNode(nm, Vector3(x, y, z), pt, ch.getOrElse(Nil))
      case m:List[_] =>
        assert(ch.isEmpty)
        
        var ix = LinearJoint.Parms(LinearJoint.X, 0f)
        var iy = LinearJoint.Parms(LinearJoint.Y, 0f) 
        var iz = LinearJoint.Parms(LinearJoint.Z, 0f)
        
        for(i <- m.asInstanceOf[List[LinMapTp]]){
          val to = i._2 match{
            case "x" => LinearJoint.X
            case "y" => LinearJoint.Y
            case "z" => LinearJoint.Z
          }
          
          i._1 match {
            case "x" => 
              ix = LinearJoint.Parms(to, i._3)
            case "y" =>
              iy = LinearJoint.Parms(to, i._3)
            case "z" =>
              iz = LinearJoint.Parms(to, i._3)
          }
        }
        
        new LinNode(nm, ix, iy, iz, pt)
    }    
  }
  
}
