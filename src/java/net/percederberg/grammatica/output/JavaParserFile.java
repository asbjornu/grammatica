/*
 * JavaParserFile.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 *
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.grammatica.output;

import java.io.IOException;
import java.util.HashMap;

import net.percederberg.grammatica.code.java.JavaClass;
import net.percederberg.grammatica.code.java.JavaComment;
import net.percederberg.grammatica.code.java.JavaConstructor;
import net.percederberg.grammatica.code.java.JavaFile;
import net.percederberg.grammatica.code.java.JavaImport;
import net.percederberg.grammatica.code.java.JavaMethod;
import net.percederberg.grammatica.code.java.JavaVariable;
import net.percederberg.grammatica.parser.ProductionPattern;
import net.percederberg.grammatica.parser.ProductionPatternAlternative;
import net.percederberg.grammatica.parser.ProductionPatternElement;

/**
 * The Java parser file generator. This class encapsulates all the
 * Java code necessary for creating a parser.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class JavaParserFile {

    /**
     * The class comment.
     */
    private static final String TYPE_COMMENT =
        "A token stream parser.";

    /**
     * The production constant comment.
     */
    private static final String PRODUCTION_COMMENT =
        "A generated production node identity constant.";
   
    /**
     * The first constructor comment.
     */
    private static final String CONSTRUCTOR1_COMMENT =
        "Creates a new parser.\n\n" +
        "@param in             the input stream to read from\n\n" +
        "@throws ParserCreationException if the parser couldn't be\n" +
        "            initialized correctly";

    /**
     * The second constructor comment.
     */
    private static final String CONSTRUCTOR2_COMMENT =
        "Creates a new parser.\n\n" +
        "@param in             the input stream to read from\n" +
        "@param analyzer       the analyzer to use while parsing\n\n" +
        "@throws ParserCreationException if the parser couldn't be\n" +
        "            initialized correctly";

    /**
     * The init method comment.
     */
    private static final String INIT_METHOD_COMMENT =
        "Initializes the parser by creating all the production patterns.\n\n" +
        "@throws ParserCreationException if the parser couldn't be\n" +
        "            initialized correctly";

    /**
     * The Java parser generator.
     */
    private JavaParserGenerator gen;

    /**
     * The tokenizer file generator.
     */
    private JavaTokenizerFile tokenizer;

    /**
     * The Java file to write.
     */
    private JavaFile file;
    
    /**
     * The Java class to write.
     */
    private JavaClass cls;
    
    /**
     * The Java class initializer method.
     */
    private JavaMethod initMethod;
    
    /**
     * A map with the production constants in this class. This map
     * is indexed with the pattern id and contains the production
     * constant name.
     */
    private HashMap constantNames = new HashMap();

    /**
     * The first available constant id number.
     */
    private int constantId = 1;

    /**
     * Creates a new parser file.
     * 
     * @param gen            the parser generator to use
     * @param tokenizer      the tokenizer file generator
     */
    public JavaParserFile(JavaParserGenerator gen, 
                          JavaTokenizerFile tokenizer) {

        int  modifiers;

        this.gen = gen;
        this.tokenizer = tokenizer;
        this.file = gen.createJavaFile();
        if (gen.getPublicAccess()) {
            modifiers = JavaClass.PUBLIC;
        } else {
            modifiers = JavaClass.PACKAGE_LOCAL;
        }
        this.cls = new JavaClass(modifiers, 
                                 gen.getBaseName() + "Parser", 
                                 "RecursiveDescentParser");
        this.initMethod = new JavaMethod(JavaMethod.PRIVATE,
                                         "createPatterns",
                                         "",
                                         "void");
        initializeCode();
    }

    /**
     * Initializes the source code objects.
     */    
    private void initializeCode() {
        JavaConstructor  constr;
        String           str;

        // Add imports
        file.addImport(new JavaImport("java.io", "Reader"));
        file.addImport(new JavaImport("net.percederberg.grammatica.parser", 
                                      "Analyzer"));
        file.addImport(new JavaImport("net.percederberg.grammatica.parser", 
                                      "ParserCreationException"));
        file.addImport(new JavaImport("net.percederberg.grammatica.parser", 
                                      "ProductionPattern"));
        file.addImport(new JavaImport("net.percederberg.grammatica.parser", 
                                      "ProductionPatternAlternative"));
        file.addImport(new JavaImport("net.percederberg.grammatica.parser", 
                                      "RecursiveDescentParser"));

        // Add class
        file.addClass(cls);
        str = TYPE_COMMENT;
        if (gen.getClassComment() != null) {
            str += "\n\n" + gen.getClassComment();
        }
        cls.addComment(new JavaComment(str));
        
        // Add file comment
        str = file.toString() + "\n\n" + gen.getFileComment();
        file.addComment(new JavaComment(JavaComment.BLOCK, str));

        // Add constructor
        constr = new JavaConstructor("Reader in");
        cls.addConstructor(constr);
        constr.addComment(new JavaComment(CONSTRUCTOR1_COMMENT));
        constr.addThrows("ParserCreationException");
        constr.addCode("super(" + tokenizer.getConstructorCall("in") + ");");
        constr.addCode("createPatterns();");
        
        // Add constructor
        constr = new JavaConstructor("Reader in, Analyzer analyzer");
        cls.addConstructor(constr);
        constr.addComment(new JavaComment(CONSTRUCTOR2_COMMENT));
        constr.addThrows("ParserCreationException");
        constr.addCode("super(" + tokenizer.getConstructorCall("in") +
                       ", analyzer);");
        constr.addCode("createPatterns();");
        
        // Add init method
        cls.addMethod(initMethod);
        initMethod.addComment(new JavaComment(INIT_METHOD_COMMENT));
        initMethod.addThrows("ParserCreationException");
        initMethod.addCode("ProductionPattern             pattern;");
        initMethod.addCode("ProductionPatternAlternative  alt;");
    }

    /**
     * Adds a production constant definition to this file.
     * 
     * @param pattern        the production pattern
     */
    public void addProductionConstant(ProductionPattern pattern) {
        String        constant;
        JavaVariable  var;
        int           modifiers;
        
        if (pattern.isSyntetic()) {
            constant = "SUBPRODUCTION_" + constantId;
            modifiers = JavaVariable.PRIVATE + JavaVariable.STATIC +
                        JavaVariable.FINAL;
            var = new JavaVariable(modifiers, 
                                   "int", 
                                   constant, 
                                   String.valueOf(constantId + 3000));
            var.addComment(new JavaComment(PRODUCTION_COMMENT));
            cls.addVariable(var);
            constantNames.put(new Integer(pattern.getId()), constant);
            constantId++;
        }
    }

    /**
     * Adds a production pattern definition to this file.
     * 
     * @param pattern        the production pattern
     * @param constants      the constants file generator
     */
    public void addProduction(ProductionPattern pattern, 
                              JavaConstantsFile constants) {
        StringBuffer  code;
        String        str;
        
        // Create new pattern
        code = new StringBuffer();
        code.append("pattern = new ProductionPattern(");
        code.append(getConstant(constants, pattern.getId()));
        code.append(",\n");
        code.append("                                \"");
        if (pattern.isSyntetic()) {
            str = getConstant(constants, pattern.getId());
            code.append(gen.getCodeStyle().getMixedCase(str, true));
        } else {
            code.append(pattern.getName());
        }
        code.append("\");");
        initMethod.addCode("");
        initMethod.addCode(code.toString());

        // Set syntetic flag
        if (pattern.isSyntetic()) {
            initMethod.addCode("pattern.setSyntetic(true);");
        }
        
        // Create pattern rules
        for (int i = 0; i < pattern.getAlternativeCount(); i++) {
            addProductionAlternative(pattern.getAlternative(i), 
                                     constants);
        }

        // Add pattern to parser
        initMethod.addCode("addPattern(pattern);");
    }

    /**
     * Adds a production pattern alternative definition to the init 
     * method.
     * 
     * @param alt            the production pattern alternative
     * @param constants      the constants file generator
     */
    private void addProductionAlternative(ProductionPatternAlternative alt, 
                                          JavaConstantsFile constants) {

        ProductionPatternElement  elem; 
        StringBuffer              code;

        initMethod.addCode("alt = new ProductionPatternAlternative();");
        for (int i = 0; i < alt.getElementCount(); i++) {
            elem = alt.getElement(i);
            code = new StringBuffer();
            code.append("alt.");
            if (elem.isToken()) {
                code.append("addToken(");
            } else {
                code.append("addProduction(");
            }
            code.append(getConstant(constants, elem.getId()));
            code.append(", ");
            code.append(elem.getMinCount());
            code.append(", ");
            if (elem.getMaxCount() == Integer.MAX_VALUE) {
                code.append("-1");
            } else {
                code.append(elem.getMaxCount());
            }
            code.append(");");
            initMethod.addCode(code.toString());
        }
        initMethod.addCode("pattern.addAlternative(alt);");
    }

    /**
     * Returns the constant name for a specified pattern or token id.
     * 
     * @param constants      the constants file
     * @param id             the pattern id
     * 
     * @return the constant name to use
     */
    private String getConstant(JavaConstantsFile constants, int id) { 
        Integer  value = new Integer(id);

        if (constantNames.containsKey(value)) {
            return (String) constantNames.get(value);
        } else {
            return constants.getConstant(id);
        }
    }

    /**
     * Writes the file source code.
     * 
     * @throws IOException if the output file couldn't be created 
     *             correctly 
     */
    public void writeCode() throws IOException {
        file.writeCode(gen.getCodeStyle());
    }
}