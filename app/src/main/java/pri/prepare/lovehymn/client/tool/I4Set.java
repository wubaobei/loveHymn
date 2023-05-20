package pri.prepare.lovehymn.client.tool;

import pri.prepare.lovehymn.server.entity.MyFile;

public interface I4Set {
    void Share(MyFile file,boolean isMp3);
    void RefreshScreenK();
    void loadPdfCall(String text);
}
