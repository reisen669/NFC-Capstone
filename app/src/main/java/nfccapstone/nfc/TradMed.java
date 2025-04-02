package nfccapstone.nfc;


public class TradMed {

    private String title;
    // the resource ID of the thumbnail image in the drawable-nodpi folder
    private int image;
    // the resource ID of the video in the raw folder
    private int video;
    // the resource ID in strings.xml
    private int altname;
    private int detail;
    private int warning;

    public TradMed(String title,int image, int video, int altname, int detail, int warning) {
        this.title = title;
        this.image = image;
        this.video = video;
        this.altname = altname;
        this.detail = detail;
        this.warning = warning;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }

    public int getVideo() {
        return video;
    }

    public int getAltName() {
        return altname;
    }

    public int getDetail() {
        return detail;
    }

    public int getWarning() {
        return warning;
    }
}

