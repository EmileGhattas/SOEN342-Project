package domain;

// A room, piece of equipment or human role that a medical service needs
public class Resource {
    private int id;
    private ResourceType type;
    private String name;

    public Resource(ResourceType type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public ResourceType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}
