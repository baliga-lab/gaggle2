package org.systemsbiology.gaggle.core.datatypes;


/**
 * A list of identifiers.
 */
public class Namelist implements GaggleData {
    private static final long serialVersionUID = 7315981527261181784L;
    private String name;
    private String species;
    private String[] names;
    private Tuple metadata;

    public Namelist() {} // no-arg ctor required

    public Namelist(String name, String species, String[] names) {
        this.name = name;
        this.species = species;
        this.names = names;
    }

    public Namelist(String species, String[] names) {
        this(null, species, names);
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() { return species; }
    public void setSpecies(String species) {
        this.species = species;
    }

    public String[] getNames() { return names; }
    public void setNames(String[] names) {
        this.names = names;
    }

    public Tuple getMetadata() { return metadata; }
    public void setMetadata(Tuple metadata) {
        this.metadata = metadata;
    }
}
