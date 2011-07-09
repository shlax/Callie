package ws.loaders.groovy.elements;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;
import ts.doc.*;
import ws.loaders.groovy.FactoryElement;
import ws.loaders.groovy.SceneBuilder;
import ws.loaders.groovy.objects.*;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import java.util.Map;

public final class TransformElement extends AbstractFactory implements Doc{

    @Override
    public String docDescription() {
        return "matrix";
    }

    @Override
    public String[] docExamples() {
        return new String[]{
            "wingAxis = _transform(rotZ:90f);",
            "_transform(x:-0.07f, y:-0.015f, z:0.044f,\n" +
             "          rotX:75.337f, rotY:-13.041f)",
        };
    }



    @Override
    public DocAction[] docActions() {
        return null;
    }

    @Override
    public DocControl[] docControl() {
        return null;
    }

    @Override
    public String docValue() {
        return "as: |position|scale|rotate|";
    }

    @Override
    public DocAttr[] docAtributes() {
        return new DocAttr[]{
            new DocAttr(null, "rotate", "rotate", "(0f,0f,0f)",null),
            new DocAttr(null, "rotX", "Float", "0f",null),
            new DocAttr(null, "rotY", "Float", "0f",null),
            new DocAttr(null, "rotZ", "Float", "0f",null),
            new DocAttr(null, "position", "point|vector", "(0f,0f,0f)",null),
            new DocAttr(null, "x", "Float", "0f",null),
            new DocAttr(null, "y", "Float", "0f",null),
            new DocAttr(null, "z", "Float", "0f",null),
            new DocAttr(null, "scale", "scale", "(1f,1f,1f)",null),
            new DocAttr(null, "scaleX", "Float", "1f",null),
            new DocAttr(null, "scaleY", "Float", "1f",null),
            new DocAttr(null, "scaleZ", "Float", "1f",null),
        };
    }

    @Override
    public final TransformObject newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        TransformObject t = new TransformObject(value, attributes);

        if(value != null && value instanceof String) t.setName((String)value);
        else if(value != null && ( value instanceof Point || value instanceof Vector) ){
            Vector3f q = ((Tuple) value).getVector3f();
            t.setX(q.x);
            t.setY(q.y);
            t.setZ(q.z);
        }else if(value != null && ( value instanceof Scale) ){
            Vector3f q = ((Tuple) value).getVector3f();
            t.setScaleX(q.x);
            t.setScaleY(q.y);
            t.setScaleZ(q.z);
        }else if(value != null && ( value instanceof Quat) ){
            Vector3f q = ((Tuple) value).getVector3f();
            t.setRotX(q.x);
            t.setRotY(q.y);
            t.setRotZ(q.z);
        }

        if(attributes != null){

            Object o = attributes.get(SceneBuilder.rotate);

            Object a = attributes.get(SceneBuilder.rotX);
            Object b = attributes.get(SceneBuilder.rotY);
            Object c = attributes.get(SceneBuilder.rotZ);

            if(o != null || a !=null || b != null || c != null){
                Vector3f v = null; // = null; // o == null ? new Vector3f() : o  (Vector3f)o;
                if(o instanceof Vector3f) v = (Vector3f)o;
                else if(o instanceof Tuple3f) v = new Vector3f((Tuple3f)o);
                else if(o instanceof Tuple) v = ((Tuple)o).getVector3f();
                //else if(o instanceof Vector) v = ((Vector)o).getVector3f();
                //else v = new Vector3f();

                if(v == null){
                    if(a != null) t.setRotX( a instanceof Float ? (Float)a : Float.parseFloat(a.toString()) );
                    if(b != null) t.setRotY( b instanceof Float ? (Float)b : Float.parseFloat(b.toString()) );
                    if(c != null) t.setRotZ( c instanceof Float ? (Float)c : Float.parseFloat(c.toString()) );
                }else {

                    if(a != null) v.x = a instanceof Float ? (Float)a : Float.parseFloat(a.toString());
                    if(b != null) v.y = b instanceof Float ? (Float)b : Float.parseFloat(b.toString());
                    if(c != null) v.z = c instanceof Float ? (Float)c : Float.parseFloat(c.toString());

                    t.setRotX(v.x);
                    t.setRotY(v.y);
                    t.setRotZ(v.z);
                }
            }

            o = attributes.get(SceneBuilder.position);

            a = attributes.get(SceneBuilder.x);
            b = attributes.get(SceneBuilder.y);
            c = attributes.get(SceneBuilder.z);

            if(o != null || a !=null || b != null || c != null){
                Vector3f v; // = null; // o == null ? new Vector3f() : o  (Vector3f)o;
                if(o instanceof Vector3f) v = (Vector3f)o;
                else if(o instanceof Tuple3f) v = new Vector3f((Tuple3f)o);
                else if(o instanceof Tuple) v = ((Tuple)o).getVector3f();
                //else if(o instanceof Vector) v = ((Vector)o).getVector3f();
                else v = new Vector3f();

                if(a != null) v.x = a instanceof Float ? (Float)a : Float.parseFloat(a.toString());
                if(b != null) v.y = b instanceof Float ? (Float)b : Float.parseFloat(b.toString());
                if(c != null) v.z = c instanceof Float ? (Float)c : Float.parseFloat(c.toString());

                t.setX(v.x);
                t.setY(v.y);
                t.setZ(v.z);
            }

            //o = attributes.get(SceneBuilder.scaleXYZ);
            o = attributes.get(SceneBuilder.scale);

            a = attributes.get(SceneBuilder.scaleX);
            b = attributes.get(SceneBuilder.scaleY);
            c = attributes.get(SceneBuilder.scaleZ);

            if(/*o != null ||*/ a !=null || b != null || c != null || o != null){
                Vector3f v; // = null; // o == null ? new Vector3f(1f, 1f, 1f) : (Vector3f)o;
                if(o instanceof Vector3f) v = (Vector3f)o;
                else if(o instanceof Tuple3f) v = new Vector3f((Tuple3f)o);
                else if(o instanceof Tuple) v = ((Tuple)o).getVector3f();
                //else if(o instanceof Vector) v = ((Vector)o).getVector3f();
                else v = new Vector3f(1f, 1f, 1f);

                if(o != null && o instanceof Float) v.scale( (Float)o );
                else if(o != null && o instanceof String) v.scale( Float.parseFloat(o.toString()) );

                if(a != null) v.x = a instanceof Float ? (Float)a : Float.parseFloat(a.toString());
                if(b != null) v.y = b instanceof Float ? (Float)b : Float.parseFloat(b.toString());
                if(c != null) v.z = c instanceof Float ? (Float)c : Float.parseFloat(c.toString());

                t.setScaleX(v.x);
                t.setScaleY(v.y);
                t.setScaleZ(v.z);
            }

            attributes.clear();
        }
        return t;
    }

    @Override
    public DocSubNode[] docSubNodes() {
        return null;
    }

    @Override
    public final void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if(child instanceof FactoryElement) if(((FactoryElement)child).isUsed())return;

        if(parent instanceof TransformObject && child instanceof Quat){
            TransformObject g = (TransformObject)parent;
            Quat so = (Quat)child;
            Vector3f tmp = so.getVector3f();
            g.setRotX(tmp.x);
            g.setRotY(tmp.y);
            g.setRotZ(tmp.z);
        }else if(parent instanceof TransformObject && child instanceof Scale){
            TransformObject g = (TransformObject)parent;
            Scale so = (Scale)child;
            Vector3f tmp = so.getVector3f();
            g.setScaleX(tmp.x);
            g.setScaleY(tmp.y);
            g.setScaleZ(tmp.z);
        }else if(parent instanceof TransformObject && child instanceof Tuple){
            TransformObject g = (TransformObject)parent;
            Tuple so = (Tuple)child;
            Vector3f tmp = so.getVector3f();
            g.setX(tmp.x);
            g.setY(tmp.y);
            g.setZ(tmp.z);
        }else System.err.println(parent+" -> "+child);
    }
}
