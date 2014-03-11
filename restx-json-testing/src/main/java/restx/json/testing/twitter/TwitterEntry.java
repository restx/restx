package restx.json.testing.twitter;

// a simple entity to be mapped to json, most of the code borrowed from jackson jsm-json-benchmark,
// which is Apache Licensed
public class TwitterEntry {
    public int id;
    public String text;
    public String from_user, to_user;
    public Integer from_user_id, to_user_id;
    public String iso_language_code;
    public String profile_image_url;
    public String created_at;
    
    public TwitterEntry() { }

    public void setId(int v) { id = v; }
    public void setText(String v) { text = v; }

    public void setFrom_user_id(Integer v) { from_user_id = v; }
    public void setTo_user_id(Integer v) { to_user_id = v; }
    
    public void setFrom_user(String v) { from_user = v; }
    public void setTo_user(String v) { to_user = v; }
    public void setIso_language_code(String v) { iso_language_code = v; }
    public void setProfile_image_url(String v) { profile_image_url = v; }
    public void setCreated_at(String v) { created_at = v; }

    public int getId() { return id; }
    public String getText() { return text; }
    
    public Integer getFrom_user_id() { return from_user_id; }
    public Integer getTo_user_id() { return to_user_id; }
    
    public String getFrom_user() { return from_user; }
    public String getTo_user() { return to_user; }
    public String getIso_language_code() { return iso_language_code; }
    public String getProfile_image_url() { return profile_image_url; }
    public String getCreated_at() { return created_at; }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("id=").append(id);
        sb.append("; Text=").append(text);

        sb.append(";from_user_id =").append(from_user_id);
        sb.append(";to_user_id =").append(to_user_id);
        sb.append(";from_user =").append(from_user);
        sb.append(";to_user =").append(to_user);
        sb.append(";iso_language_code =").append(iso_language_code);
        sb.append(";profile_image_url =").append(profile_image_url);
        sb.append(";created_at =").append(created_at);
        
        sb.append("}");
        return sb.toString();
    }

}