package ws.loaders.groovy.elements;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;
import ws.loaders.groovy.SceneBuilder;
import ws.loaders.groovy.objects.BhoneSkinFrameObject;
import ws.loaders.tools.SoundLoader;
import ws.loaders.tools.joint.BhoneFrameLoader;

import java.util.Map;

public final class BhoneSkinFrameElement extends AbstractFactory {
    private final BhoneFrameLoader bhoneFrameLoader;
    private final SoundLoader soundLoader; // = new SoundLoader();

    public BhoneSkinFrameElement(BhoneFrameLoader bhoneFrameLoader, SoundLoader soundLoader) {
        this.bhoneFrameLoader = bhoneFrameLoader;        
        this.soundLoader = soundLoader;
    }

    @Override
    public final BhoneSkinFrameObject newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        BhoneSkinFrameObject o = new BhoneSkinFrameObject(bhoneFrameLoader, soundLoader);

        if(value != null) o.setBhoneFrame(value.toString() );
        if(attributes != null){
            Object tmp = attributes.get(SceneBuilder.name);
            if(tmp != null) o.setName(tmp.toString());

            tmp = attributes.get(SceneBuilder.file);
            if(tmp != null) o.setBhoneFrame(tmp.toString() );

            tmp = attributes.get(SceneBuilder.clip);
            if(tmp != null) o.setClip(tmp.toString() );
            
            attributes.clear();
        }

        return o;
    }
}
