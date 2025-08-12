package pers.fz.mvvm.widget.customview.round;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fz on 2023/5/23 16:33
 * describe :
 */
public class SpanContainer {
    public List<Object> spans;
    public int start;
    public int end;
    public int flag;

    public SpanContainer(List<Object> spans, int start, int end, int flag) {
        this.spans = spans;
        this.start = start;
        this.end = end;
        this.flag = flag;
    }

    public SpanContainer(Object spans, int start, int end, int flag) {
        this.spans = new ArrayList<>();
        this.spans.add(spans);
        this.start = start;
        this.end = end;
        this.flag = flag;
    }
}
