header {
package org.argouml.uml.reveng.classfile;

import org.argouml.uml.reveng.java.*;
import java.util.*;
}   
/*
 * Java Classfile tree parser.
 *
 * Contributing authors:
 *     Andreas Rueckert <a_rueckert@gmx.net>
 */

/*************************************
 * A tree parser for a Java classfile.
 *************************************/
class ClassfileTreeParser extends TreeParser;

options {
        importVocab = Classfile;
	defaultErrorHandler = false;     // Don't generate parser error handlers
}   

{
    // The modeller to create the meta model objects.
    private Modeller _modeller;

    /**
     * Return the modeller of this parser.
     *
     * @return The modeller of this parser.
     */
    public final Modeller getModeller() {
        return _modeller;
    }

    /**
     * Set the modeller of this parser.
     *
     * @param modeller The new modeller of this parser.
     */
    public final void setModeller( Modeller modeller) {
        _modeller = modeller;
    }

    /**
     * Split class and package name and set package.
     * 
     * @param classname The fully qualified classname.
     *
     * @return The class name.
     */
    private final String splitPackageFromClass(String classname) {
	int lastDot = classname.lastIndexOf('.');
	if(lastDot != -1) {
	    getModeller().addPackage(classname.substring(0,lastDot));
	    classname = classname.substring(lastDot + 1);
	}
	return classname;
    }
}

// A entire classfile
classfile[Modeller modeller] 
{ setModeller(modeller); }
	: magic_number
	  version_number
	  typeDefinition
 	  attribute_block
	  method_block
	    {  getModeller().popClassifier(); }
	;

magic_number
	: MAGIC 
	;

version_number
	: VERSION 
	; 

typeDefinition
{
  short modifiers=0;
  String class_name=null;
  String superclass_name=null;
  List<String> interfaces = new ArrayList<String>();
}
	: #( INTERFACE_DEF 
	     modifiers=access_modifiers 
             class_name=class_info 
             #(EXTENDS_CLAUSE interface_block[interfaces])
           )
	     {
               getModeller().addComponent();
	       getModeller().addInterface( splitPackageFromClass(class_name), modifiers, interfaces, null);
             }
	  | 
          #( CLASS_DEF 
             modifiers=access_modifiers 
             class_name=class_info 
             #(EXTENDS_CLAUSE superclass_name=class_info) 
             #(IMPLEMENTS_CLAUSE interface_block[interfaces])
           )
	     {
	       if( "java.lang.Object".equals(superclass_name)) {
		   superclass_name=null;  
	       }
               getModeller().addComponent();
	       getModeller().addClass( splitPackageFromClass(class_name), modifiers, superclass_name, interfaces, null);
	     }
	;

access_modifiers returns [short modifiers]
	: ACCESS_MODIFIERS { modifiers=((ShortAST)#ACCESS_MODIFIERS).getShortValue(); }
	;

class_info returns [String name]
	: IDENT { name = #IDENT.getText(); }
	;

// The interfaces a class implements
interface_block[List<String> interfaces]
        : ( IDENT { interfaces.add( #IDENT.getText()); } )*
        ;

// The block with the class vars.
// (Don't be confused, if you are familiar with the classfile format. This block
// actually holds the data of the field block, but I renamed it to make it easier
// for newbies to classfiles.)
attribute_block
	: ( attribute_info )*  // This block holds the info on all the attributes.
	;

// Info on one class attributes (variables).
attribute_info
	: VARIABLE_DEF ACCESS_MODIFIERS TYPE IDENT
  	    { // Add the attribute to the model element, that holds
	      // the class/interface info.
	      getModeller().addAttribute( ((ShortAST)#ACCESS_MODIFIERS).getShortValue(), 
					  #TYPE.getText(),
					  #IDENT.getText(),
					  null,	     // I parse no initializers yet.
					  null);     // And there's no javadoc info available.
	    }
	;

method_block
	: ( 
            ctorDef
	    | methodDecl 
          )*
	;

// A constructor definition
ctorDef
{ List<ParameterDeclaration> params = null; }
	: #(CTOR_DEF ACCESS_MODIFIERS IDENT params=parameters (exceptions)? )
	  {
	    getModeller().addOperation( ((ShortAST)#ACCESS_MODIFIERS).getShortValue(),
					null,
					#IDENT.getText(),
					params,
					null);
  	  }
	;

// A method declaration
methodDecl
{ List<ParameterDeclaration> params = null; }
	: #(METHOD_DEF ACCESS_MODIFIERS TYPE IDENT params=parameters (exceptions)? )
	  {
	    getModeller().addOperation( ((ShortAST)#ACCESS_MODIFIERS).getShortValue(),
					#TYPE.getText(),
					#IDENT.getText(),
					params,
					null);
  	  }
	;

// A parameter list
parameters returns [List<ParameterDeclaration> params]
{ 
  params = new ArrayList<ParameterDeclaration>(); 
  ParameterDeclaration currentParam = null;
}
	: #( PARAMETERS ( currentParam=parameterDef {params.add(currentParam);} )* )
	;

// A single parameter
parameterDef returns [ParameterDeclaration param]
{ param = null; }
	: #( PARAMETER_DEF ACCESS_MODIFIERS TYPE IDENT) 
	  {
	   param = new ParameterDeclaration(
	              ((ShortAST)#ACCESS_MODIFIERS).getShortValue(),
                      #TYPE.getText(),
                      #IDENT.getText());
	  }
	;

// A list with thrown exceptions
exceptions
	: #( THROWS ( IDENT )* )
	;






