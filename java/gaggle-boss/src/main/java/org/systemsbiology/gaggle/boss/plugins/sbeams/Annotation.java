package org.systemsbiology.gaggle.boss.plugins.sbeams;

public class Annotation {
  private String orf;
  private String geneSymbol;
  private String ecNumbers;
  private String annotation;
  private String domainHitsURL;

    public Annotation(String orf, String geneSymbol, String ecNumbers,
                      String annotation, String domainHitsURL) {
        this.orf = orf;
        this.geneSymbol = geneSymbol;
        this.ecNumbers = ecNumbers;
        this.annotation = annotation;
        this.domainHitsURL = domainHitsURL;
    }

    public String orf() { return orf; }
    public String geneSymbol() { return geneSymbol; }
    public String ecNumbers() { return ecNumbers; }
    public String annotation() { return annotation; }
    public String domainHitsURL() { return domainHitsURL; }
}
