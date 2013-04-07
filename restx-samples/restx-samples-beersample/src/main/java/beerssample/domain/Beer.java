package beerssample.domain;

/**
 * User: xavierhanin
 * Date: 4/6/13
 * Time: 5:09 PM
 *
 *
 {
   "name": "D. Carnegie and Company Porter",
   "abv": 5.5,
   "ibu": 0,
   "srm": 0,
   "upc": 0,
   "type": "beer",
   "brewery_id": "pripps_ringnes_bryggerier",
   "updated": "2010-07-22 20:00:20",
   "description": "",
   "style": "American-Style Stout",
   "category": "North American Ale"
 }
 */
public class Beer {
    private String id;
    private String name;

    private String type;
    private String brewery_id;
    private String updated;
    private String description;

    @Override
    public String toString() {
        return "Beer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", brewery_id='" + brewery_id + '\'' +
                ", updated='" + updated + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getBrewery_id() {
        return brewery_id;
    }

    public void setBrewery_id(String brewery_id) {
        this.brewery_id = brewery_id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category;
}
