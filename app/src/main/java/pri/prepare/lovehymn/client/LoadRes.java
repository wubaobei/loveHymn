package pri.prepare.lovehymn.client;

public class LoadRes {
    public LoadRes(String shortName, String chineseName, boolean load, boolean isDir, String path, String readMe, String daily) {
        this.shortName = shortName;
        this.fullNane = chineseName;
        this.load = load;
        this.isDir = isDir;
        this.path = path;
        this.readMe = readMe;
        this.daily = daily;
    }

    public String path;
    public String shortName;
    public String fullNane;
    public boolean load;
    public boolean isDir;
    public String readMe;
    public String daily;
}
