package ws.loaders.groovy.objects;

import java.util.Map;

public abstract class SimpleInterpolator extends Interpolator {

    protected SimpleInterpolator(Object value, Map attributes) {
        super(value, attributes);
    }

    protected Float max = null;
    protected Float min = null;

    public final void setMax(Float max) {
        this.max = max;
    }

    public final void setMin(Float min) {
        this.min = min;
    }
}
