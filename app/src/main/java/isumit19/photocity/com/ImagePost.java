package isumit19.photocity.com;

import java.util.Date;

public class ImagePost extends PostId{


    private String  desc;
    private String image_url;
    private String thumb_url;
    private String user_id;
    private Date timestamp;

    public ImagePost(String desc, String image_url, String thumb_url, String user_id, Date timestamp) {
        this.desc = desc;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.user_id = user_id;
        this.timestamp=timestamp;
    }


    public ImagePost(){

    }


    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

}
