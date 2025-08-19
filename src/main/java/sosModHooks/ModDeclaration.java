package sosModHooks;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a mod's declaration to the compatibility framework.
 * Contains metadata about the mod and its modifications.
 */
public final class ModDeclaration {
    
    @Getter
    private final String id;
    
    @Getter
    private final String name;
    
    @Getter
    private final String version;
    
    @Getter
    @Setter
    private String description;
    
    @Getter
    @Setter
    private String author;
    
    @Getter
    @Setter
    private String website;
    
    public ModDeclaration(String id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }
    
    public ModDeclaration(String id, String name, String version, String description) {
        this(id, name, version);
        this.description = description;
    }
    
    public ModDeclaration(String id, String name, String version, String description, String author) {
        this(id, name, version, description);
        this.author = author;
    }
    
    @Override
    public String toString() {
        return "ModDeclaration{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
