package liveevaluation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;

import evaluationbasics.DiagnostedMethodClass;
import evaluationbasics.EvaluationTools;
import evaluationbasics.NoValidClassException;


public class EvaluationHelper {
	
	public static Object runMethodOnParams(DiagnostedMethodClass dcMethod, Object[] pTestArgs) throws Exception{
		//Falls die Methode kompilierbar ist
		if(dcMethod.isValidClass()){
			//Parameterbearbeitung		
			if(!(pTestArgs==null || pTestArgs.length==0)){
				Class<?>[] pClassType=dcMethod.getMainMethod().getParameterTypes();
				if(pClassType.length!=pTestArgs.length)
					throw new Exception("5");
				//Konvertierung in richtige Dateitypen			
				for(int i=0;i<pTestArgs.length;i++)
					pTestArgs[i]=EvaluationHelper.stringToType(pClassType[i],pTestArgs[i].toString());
			}
			try{
				return EvaluationTools.callMethod(dcMethod, pTestArgs);				
			//Hauptsaechlich StackOverFlowError:
			}catch(InvocationTargetException e){
				return e.getTargetException();
			//Normale Exceptions:
			}catch(Exception e){
				return e;
			}
		}
		throw new NoValidClassException("Given DiagnostedMethodClass is not valid");
	}	
	
	/*
	 * Konvertiert die ParamsXML in ein Array
	 */
	public static Object[] paramsToArray(Element params){
		List<Element> param=params.getChildren();
		Object[] zReturn=new Object[param.size()];
		for(int j=0;j<param.size();j++)
			zReturn[j]=param.get(j).getValue();
		return zReturn;		
	}
	
	/*
	 * Die Methode versucht einen als String gegebenen Wert in einen spezifischen Typen umzuwandeln.
	 * Bisher unterstuetze Datentypen:
	 * 	Eindemensinale Arrays unterstuetzter Datentypen
	 *	Byte
	 *	Double
	 *	Short
	 *	Integer
	 *	Long
	 *	Float
	 *	Double
	 *	String
	 *	Boolean
	 *
	 * Sofern der Typ nicht unterstuetzt wird, wird der String zurueck gegeben.
	 * 
	 * Parameters:
	 * 		pClass - Typ in den der String verwandelt werden soll
	 * 		pParam - String in welcher umgewandelt werden soll
	 */
	private static final Object stringToType(Class<?> pClass, String pParam){
		if(pClass.isArray()){									//Eindimensionale Arrays //Trennzeichen ist ,
			String[] sArray=pParam.split(",");
			Object objArray= Array.newInstance(pClass.getComponentType(), sArray.length);
			for(int i=0;i<sArray.length;i++)
				Array.set(objArray,i,stringToType(pClass.getComponentType(),sArray[i]));
			return objArray; 
		}
		else if(pClass==Byte.class || pClass==byte.class)		//Byte
			return Byte.parseByte(pParam);
		else if(pClass==Double.class || pClass==double.class)	//Double
			return pParam.charAt(0);
		else if(pClass==Short.class || pClass==short.class)		//Short
			return Short.parseShort(pParam);			
		else if(pClass==Integer.class || pClass==int.class)		//Integer
			return Integer.parseInt(pParam);
		else if(pClass==Long.class || pClass==long.class) 		//Long
			return Long.parseLong(pParam);
		else if(pClass==Float.class || pClass==float.class) 	//Float
			return Float.parseFloat(pParam);
		else if(pClass==Double.class || pClass==double.class) 	//Double
			return Double.parseDouble(pParam);
		else if(pClass==String.class)							//String
			return pParam;
		else if(pClass==Boolean.class || pClass==boolean.class)	//Boolean
			return Boolean.parseBoolean(pParam);	
		else													//unknown type
			return pParam;
	}
	
	
	/*
	 * Streambehandlung
	 */	
	public static final void setStringToOutputStream(OutputStream out, String output){
		try {
			out.write(output.getBytes());
	    	out.flush();
		} catch (IOException e) {}	//FEHLER
	}

	public static final String getStringFromInputStream(InputStream in) {		
		byte[] b=new byte[1000];
		String sReturn="";
		int i=0;
		try {
			while(i!=-1){
				i=in.read(b);
				if(i==b.length){
					sReturn=sReturn+new String(b);
				}else if(i<b.length && i!=-1)
					return sReturn+new String(Arrays.copyOf(b,i));
			}
		} catch (IOException e) {} //FEHLER
		return sReturn;	
	}
	

}
