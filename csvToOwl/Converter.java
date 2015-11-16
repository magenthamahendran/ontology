import java.io.*;
import java.util.*;

public class Converter {

    private static class Element implements Comparable<Element> {
	private String level;
	private String archiveName;
	private String sortCode;
	private String refNo;
	private String extent;
	private String title;
	private String date;
	private String description;
	private String access;
	private String copyright;
	private List<Element> partOf;
	private List<Element> composedOf;

	private ArrayList<String> convert(String line) {
	    ArrayList<String> tokens = new ArrayList<String>();
	    boolean insideComa = true;
	    int last = 0;
	    for (int i = 0; i < line.length(); ++i) {
		if (line.charAt(i) == ',' && insideComa) {
		    if (line.charAt(last) == '"') {
			tokens.add(line.substring(last + 1, i - 1));
		    } else {
			tokens.add(line.substring(last, i));
		    }
		    last = i + 1;
		} else if (line.charAt(i) == '"') {
		    insideComa = !insideComa;
		}
	    }
	    if (last < line.length() && line.charAt(last) == '"') {
		tokens.add(line.substring(last + 1, line.length() - 1));
	    } else {
		tokens.add(line.substring(last));
	    }
	    return tokens;
	}

	private ArrayList<String> tokens;

	public Element(String line) {
	    partOf = new ArrayList<Element>();
	    composedOf = new ArrayList<Element>();
	    tokens = convert(line);
	    level = tokens.get(0);
	    archiveName = tokens.get(1);
	    sortCode = tokens.get(2);
	    refNo = tokens.get(3);
	    extent = tokens.get(4);
	    title = tokens.get(5);
	    date = tokens.get(6);
	    description = tokens.get(7);
	    access = tokens.get(8);
	    copyright = tokens.get(9);
	}

	public String getParent() {
	    for (int i = sortCode.length() - 1; i >= 0; --i) {
		if (sortCode.charAt(i) == '/') {
		    return sortCode.substring(0, i);
		}
	    }
	    return null;
	}

	public int compareTo(Element element) {
	    return sortCode.compareTo(element.sortCode);
	}

	public void printData() {
	    String[] property = { "ArchiveName",  "sortCode", "RefNo", "Extent", "title", "Date", "Description", "AccessRight", "CopyRight" };
	    for (int j = 1, i = 0; i < property.length; ++j) {
		if (tokens.get(j).length() == 0) {
		    continue;
		}
		String value = tokens.get(j);
		value = value.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
		value = value.replaceAll("\\r", "").replaceAll("\\n", "");
		System.out.println("<DataPropertyAssertion>");
		System.out.println("\t<DataProperty IRI=\"#" + property[i] + "\"/>");
		System.out.println("\t<NamedIndividual IRI=\"#" + sortCode + "\"/>");
		System.out.println("\t<Literal datatypeIRI=\"&xsd;string\">" + value + "</Literal>");
		System.out.println("</DataPropertyAssertion>");
		++i;
	    }
	}
    }

   public static void main(String[] args) {
	try {
	    String filename = "data.csv";
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    TreeMap<String, Element> elements = new TreeMap<String, Element>();
	    boolean first = true;
	    while ((line = reader.readLine()) != null) {
		if (first) {
		    first = false;
		    continue;
		}
		Element element = new Element(line);
		elements.put(element.sortCode, element);
	    }
	    for (String key : elements.keySet()) {
		Element element = elements.get(key);
		Element parent = elements.get(element.getParent());
		if (parent != null) {
		    parent.composedOf.add(element);
		    element.partOf.add(parent);
		}
	    }
	    for (String key : elements.keySet()) {
		Element element = elements.get(key);
		System.out.println("<Declaration>");
		System.out.println("\t<NamedIndividual IRI=\"#" + element.sortCode + "\"/>");
		System.out.println("</Declaration>");
		System.out.println("<ClassAssertion>");
		System.out.println("\t<Class IRI=\"#" + element.level + "\"/>");
		System.out.println("\t<NamedIndividual IRI=\"#" + element.sortCode + "\"/>");
		System.out.println("</ClassAssertion>");
	    }
	    for (String key : elements.keySet()) {
		Element element = elements.get(key);
		for (Element parent : element.partOf) {
		    System.out.println("<ObjectPropertyAssertion>");
		    System.out.println("\t<ObjectProperty IRI=\"#partOf\"/>");
		    System.out.println("\t<NamedIndividual IRI=\"#" + element.sortCode + "\"/>");
		    System.out.println("\t<NamedIndividual IRI=\"#" + parent.sortCode + "\"/>");
		    System.out.println("</ObjectPropertyAssertion>");
		}
		for (Element child : element.composedOf) {
                    System.out.println("<ObjectPropertyAssertion>");
                    System.out.println("\t<ObjectProperty IRI=\"#composedOf\"/>");
                    System.out.println("\t<NamedIndividual IRI=\"#" + element.sortCode + "\"/>");
                    System.out.println("\t<NamedIndividual IRI=\"#" + child.sortCode + "\"/>");
                    System.out.println("</ObjectPropertyAssertion>");
		}
		element.printData();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
