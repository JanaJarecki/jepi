package evaluationbasics.AnalysisHelpers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import evaluationbasics.Exceptions.TooManyMethodsException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.WhileStatement;



/**
 * Die Klasse bietet Methoden zur Rekursions-/Iterationsueberpruefung sowie zur Suche von sicherheitskritischen Methoden.
 * 
 * @author Roman Bange
 *
 */
public class ASTHelper {
	
	/**
	 * Diese Methode gibt die Art der Programmierung zurueck.
	 * [0] -> Rekursion {true/false}
	 * [1] -> Iteration {true/false}
	 * Erkennung der Rekursion geschieht indem der Methodenname sowie die Anzahl der Parameter verglichen wird
	 * Erkennung der Iteration wird erkannt wenn Schleifen benutzt werden.
	 * 
	 * @param sMethod Quelltext der einzelnen Methode
	 * @return Rueckgabearray. Formatierung siehe Beschreibung
	 * @throws TooManyMethodsException sofern mehrere Methoden uebergeben wurden
	 */
	public static Map<String,Boolean> getMethodType(String sMethod) throws TooManyMethodsException{
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(("public class A{"+sMethod+"}").toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		MethodTypeVisitor v=new MethodTypeVisitor();
		cu.accept(v);
		if(v.methodCount()!=1)
			throw new TooManyMethodsException("Unable to identify the main Method");

		Map<String,Boolean> methodType = new HashMap<String,Boolean>();
		methodType.put("recursive",v.isRecursive());
		methodType.put("loop",v.hasLoop());
		return methodType;
	}
	
	/**
	 * Diese Methode durchsucht die gegebene Methode nach kritischen Methodenaufrufen wie zB System.exit(); bevor die Methode ausgefuehrt werden muss.
	 * 
	 * @param sMethod Quelltext der einzelnen Methode
	 * @param forbiddenMethods Array der verbotenen Methoden
	 * @return true, sofern sicherheitskritsch /false wenn nicht
	 */
	public static boolean isSecurityLeak(String sMethod,String [] forbiddenMethods){
		if(sMethod==null || sMethod.equals("") || forbiddenMethods==null || forbiddenMethods.equals(""))
			return false;
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(("public class A{"+sMethod+"}").toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		SecurityVisitor v=new SecurityVisitor(forbiddenMethods);
		cu.accept(v);
		
		return v.usedForbiddenMethods();
	}
	
	private static class MethodTypeVisitor extends ASTVisitor{		
		//Ausgabe
		public boolean isRecursive(){return this.methodCount()==1 && declaredMethods.getFirst().isRecursive;}
		public boolean hasLoop(){return whileCount>0 || dowhileCount>0 || forCount>0;}
		public int methodCount(){return declaredMethods.size();}
		
		//Analyse
		private byte whileCount=0;
		private byte dowhileCount=0;
		private byte forCount=0;
		private LinkedList<DeclaredMethod> declaredMethods=new LinkedList<DeclaredMethod>();
		private class DeclaredMethod{
			public DeclaredMethod(String pName,int pParamCount){sName=pName;iParamCount=pParamCount;}
			public boolean isRecursive=false;
			public String sName="";
			public int iParamCount=0;
		}
		
		//visit
		public boolean visit(WhileStatement node){whileCount++;return true;}
		public boolean visit(ForStatement node){forCount++;return true;}
		public boolean visit(DoStatement node){dowhileCount++;return true;}
		public boolean visit(MethodDeclaration node){
			if(!node.isConstructor())
				declaredMethods.add(new DeclaredMethod(node.getName().toString(),node.parameters().size()));
			return true;	
		}		
		public boolean visit(MethodInvocation node){
			for(DeclaredMethod dc: declaredMethods)
				if(dc.sName.equals(node.getName().toString()) && dc.iParamCount==node.arguments().size())
					dc.isRecursive=true;
			return false;				
		}
	}
	
	
	private static class SecurityVisitor extends ASTVisitor{
		private final String[] forbiddenMethods;		
		private LinkedList<String> usedForbiddenMethods=new LinkedList<String>();
		
		public SecurityVisitor(String [] pForbiddenMethods){
			forbiddenMethods=pForbiddenMethods;
		}
		
		public boolean visit(MethodInvocation node){
			for(String forbiddenMethod : forbiddenMethods)
				if(node.getName().toString().equals(forbiddenMethod))
					usedForbiddenMethods.add(forbiddenMethod);
			return true;				
		}
		
		@SuppressWarnings("unused")
		public String[] getUsedForbiddenMethods(){
			if(this.usedForbiddenMethods()){
				String[] sReturn=new String[usedForbiddenMethods.size()];
				int i=0;
				for(String fM: usedForbiddenMethods){
					sReturn[i]=fM;
					i++;
				}
				return sReturn;
			}else
				return null;
			
		}
		@SuppressWarnings("unused")
		public int getCountOfUsedForbiddenMethods(){
			return usedForbiddenMethods.size();
		}
		public boolean usedForbiddenMethods(){
			return usedForbiddenMethods.size() > 0;
		}
		
	}

}
