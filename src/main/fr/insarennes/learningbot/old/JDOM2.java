package fr.insarennes.learningbot.old;
import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class JDOM2
{
   static Document document;
   static Element racine;

   public static void main(String[] args)
   {
      //On crée une instance de SAXBuilder
      SAXBuilder sxb = new SAXBuilder();
      try
      {
         //On crée un nouveau document JDOM avec en argument le fichier XML
         //Le parsing est terminé ;)
         document = sxb.build(new File("./xml/test.tree.xml"));
      }
      catch(Exception e){}

      //On initialise un nouvel élément racine avec l'élément racine du document.
      racine = document.getRootElement();

      //Méthode définie dans la partie 3.2. de cet article
      afficheALL();
   }
   
   static void afficheALL()
   {
      //On crée une List contenant tous les noeuds "etudiant" de l'Element racine
      Element e = racine.getChild("tree").getChild("node");
      
      parcoursArbre(e);
   }
   
   //parcours effectué en préfixe ici
   static void parcoursArbre(Element e) {
	   String s = "";
	   if (e.getAttributeValue("type").equals("node")) {
		   //l'élément est un noeud
		   Element question = e.getChild("question");
		   s += question.getAttributeValue("name") + "<";
		   s += question.getAttributeValue("patron") + " ?";
		   
		   System.out.println(s);
		   
		   parcoursArbre(e.getChildren("node").get(0));
		   parcoursArbre(e.getChildren("node").get(1));
	   }
	   else if (e.getAttributeValue("type").equals("leaf")) {
		   //l'élément est une feuille
		   Element pop = e.getChild("population");
		   if (pop.getAttribute("L0") != null)
			   s += "L0 : " + pop.getAttributeValue("L0") + " ";
		   if (pop.getAttribute("L1") != null)
			   s += "L1 : " + pop.getAttributeValue("L1") + " ";
		   if (pop.getAttribute("L2") != null)
			   s += "L2 : " + pop.getAttributeValue("L2") + " ";
		   
		   System.out.println(s);
	   }
   }
		   
}
