package ws.loaders.tools;

import ws.ResourceHandle;

import javax.media.j3d.Texture;
import java.net.MalformedURLException;
import java.util.HashMap;

public final class TextureLoader {

    /*private final HashMap<File, Texture> map = new HashMap<File, Texture>();
    public final Texture getTexture(File name){
		Texture tmp = map.get(name);
		if(tmp != null) return tmp;

        com.sun.j3d.utils.image.TextureLoader loader = new com.sun.j3d.utils.image.TextureLoader(name.getAbsolutePath(), null);
        tmp = loader.getTexture();

        map.put(name, tmp);
		return tmp;
	}*/

    private final HashMap<String, Texture> mapFormat = new HashMap<String, Texture>();
    public final Texture getTexture(String name, String format, int flags){
        //System.out.println(format);
        String key = flags+"|"+format+"|"+name;
		Texture tmp = mapFormat.get(key);
		if(tmp != null) return tmp;

        try {
            com.sun.j3d.utils.image.TextureLoader loader = new com.sun.j3d.utils.image.TextureLoader(ResourceHandle.getURL(name), format, flags, null);
            tmp = loader.getTexture();

            mapFormat.put(key, tmp);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
		return tmp;
	}

/*    private final HashMap<File, Texture> mapMulti = new HashMap<File, Texture>();
    public final  Texture getMultiTexture(File name){
		Texture tmp = mapMulti.get(name);
		if(tmp != null) return tmp;

        com.sun.j3d.utils.image.TextureLoader loader = new com.sun.j3d.utils.image.TextureLoader(name.getAbsolutePath(), com.sun.j3d.utils.image.TextureLoader.GENERATE_MIPMAP, null);
        tmp = loader.getTexture();
        tmp.setMinFilter(Texture.MULTI_LEVEL_LINEAR);

        mapMulti.put(name, tmp);
		return tmp;
	} */

}
