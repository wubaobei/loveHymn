package pri.prepare.lovehymn.client.tool;

public class TipStruct {
    public Integer resId;
    public String text;
    public boolean hideInReadMe;

    public TipStruct(String msg, boolean hide) {
        resId = null;
        text = msg;
        this.hideInReadMe = hide;
    }

    public TipStruct(String msg) {
        resId = null;
        text = msg;
        this.hideInReadMe = false;
    }

    public TipStruct(int resId, String msg) {
        this.resId = resId;
        this.text = msg;
    }
}
