package uk.ac.ncl.intbio.core.io.rdf;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uk.ac.ncl.intbio.core.datatree.*;
import uk.ac.ncl.intbio.core.io.CoreIoException;
import uk.ac.ncl.intbio.core.io.IoWriter;
import static uk.ac.ncl.intbio.core.io.rdf.RdfTerms.*;

public class RdfIo{

	public IoWriter<QName> createIoWriter(final XMLStreamWriter writer)
	{
		return new IoWriter<QName>() {
			
			@Override
			public void write(DocumentRoot<QName> document) throws CoreIoException {
				try
				{
					writer.writeStartDocument();

					writeStartElement(RDF);
					setPrefix(rdf);
					writeNamespace(rdf);
					
					for(NamespaceBinding nb : document.getNamespaceBindings()) {
						setPrefix(nb);
						writeNamespace(nb);
					}

          for (NamedProperty<QName, Literal> properties : document.getProperties()) {
            // todo: we do nothing with these right now
          }


          for (TopLevelDocument<QName> child : document.getTopLevelDocuments())
					{
						write(child);
					}

					writer.writeEndElement();
					writer.writeEndDocument();
				}
				catch(XMLStreamException xse)
				{
					throw new CoreIoException(xse);
				}
				
			}

			private void write(IdentifiableDocument<QName, PropertyValue> doc) throws XMLStreamException {
        writeStartElement(doc.getType());
        writeAttribute(rdfAbout, doc.getIdentity());

        for (NamedProperty<QName, PropertyValue> property : doc.getProperties()) {
          write(property);
        }

        writer.writeEndElement();
			}

      private void write(NamedProperty<QName, PropertyValue> property) throws XMLStreamException {
    	if(property.getValue() instanceof Literal) {
    		Literal value = (Literal) property.getValue();
    		if(isEmptyElementValue(value)) {
    			writeEmptyElement(property.getName());
                write(value);
    		} else {
    		    writeStartElement(property.getName());
                write(value);
                writer.writeEndElement();
    		}
    	} else if(property.getValue() instanceof Datatree.NestedDocuments) {
    		Datatree.NestedDocuments<QName> docs = (Datatree.NestedDocuments<QName>) property.getValue();
    		for(NestedDocument<QName> doc : docs.getDocuments()) {
                writeStartElement(property.getName());
    			write(doc);
                writer.writeEndElement();
    		}
    	} else {
    		throw new IllegalStateException("Unknown type of property value for: " + property.getValue());
    	}
      }
      
      private boolean isEmptyElementValue(Literal literal) {
    	  return literal instanceof Literal.QNameLiteral || literal instanceof Literal.UriLiteral;
      }

      private void write(Literal literal) throws XMLStreamException {
        if(literal instanceof Literal.StringLiteral) {
          writer.writeCharacters(((Literal.StringLiteral) literal).getValue());
        } else if (literal instanceof Literal.IntegerLiteral) {
        	writer.writeCharacters(((Literal.IntegerLiteral) literal).getValue().toString());
        } else if(literal instanceof Literal.QNameLiteral) {
        	Literal.QNameLiteral ql = (Literal.QNameLiteral) literal;
        	writeAttribute(rdfResource, ql.getValue());
        } 
        else if(literal instanceof Literal.UriLiteral) {
        	Literal.UriLiteral ul = (Literal.UriLiteral) literal;
        	writeAttribute(rdfResource, ul.getValue().toString());
        } 
        
        else {
          throw new IllegalStateException("Unknown type of literal: " + literal.getClass().getName() +
        		  " extends " + literal.getClass().getInterfaces()[0].getName());
        }
      }
      
      private void writeEmptyElement(QName tagName) throws XMLStreamException {
    	  writer.writeEmptyElement(tagName.getPrefix(), tagName.getLocalPart(), tagName.getNamespaceURI());
      }

      private void writeStartElement(QName tagName) throws XMLStreamException {
        writer.writeStartElement(tagName.getPrefix(), tagName.getLocalPart(), tagName.getNamespaceURI());
      }

      private void setPrefix(NamespaceBinding binding) throws XMLStreamException {
        writer.setPrefix(binding.getPrefix(), binding.getNamespaceURI());
      }

      private void writeNamespace(NamespaceBinding binding) throws XMLStreamException {
        writer.writeNamespace(binding.getPrefix(), binding.getNamespaceURI());
      }

      private void writeAttribute(QName attrName, QName attrValue) throws XMLStreamException {
        writer.writeAttribute(
                attrName.getPrefix(),
                attrName.getNamespaceURI(),
                attrName.getLocalPart(),
                attrValue.getNamespaceURI() + attrValue.getLocalPart());
      }
      
      private void writeAttribute(QName attrName, String attrValue) throws XMLStreamException {
          writer.writeAttribute(
                  attrName.getPrefix(),
                  attrName.getNamespaceURI(),
                  attrName.getLocalPart(),
                  attrValue);
        }
      
		};
	}
}