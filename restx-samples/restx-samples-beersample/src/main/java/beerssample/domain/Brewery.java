package beerssample.domain;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 4/6/13
 * Time: 5:09 PM
 *
 *
 {
   "name": "21st Amendment Brewery Cafe",
   "city": "San Francisco",
   "state": "California",
   "code": "94107",
   "country": "United States",
   "phone": "1-415-369-0900",
   "website": "http://www.21st-amendment.com/",
   "type": "brewery",
   "updated": "2010-10-24 13:54:07",
   "description": "The 21st Amendment Brewery offers a variety of award winning house made brews and American grilled cuisine in a comfortable loft like setting. Join us before and after Giants baseball games in our outdoor beer garden. A great location for functions and parties in our semi-private Brewers Loft. See you soon at the 21A!",
   "address": [
     "563 Second Street"
   ],
   "geo": {
     "accuracy": "ROOFTOP",
     "lat": 37.7825,
     "lng": -122.393
   }
 }
 */
public class Brewery {
    private String id;
    private String name;
    private String city;
    private String state;
    private String code;
    private String country;
    private String phone;
    private String website;
    private String type;
    private String updated;
    private String description;
    private List<String> address;

    @Override
    public String toString() {
        return "Brewery{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", code='" + code + '\'' +
                ", country='" + country + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", type='" + type + '\'' +
                ", updated='" + updated + '\'' +
                ", description='" + description + '\'' +
                ", address=" + address +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }
}
