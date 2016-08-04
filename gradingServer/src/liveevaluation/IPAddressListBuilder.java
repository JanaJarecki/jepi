package liveevaluation;

import java.util.LinkedList;
/**
 * Dient zur Unterstuetzung der LiveEvaluationMain-Klasse.
 * Aufgaben:
 * Formatueberpruefung der Eingaben
 * Zusammenfuehren von nur korrekten Addressen
 * 
 * @author roman
 *
 */
public final class IPAddressListBuilder {
	public final static byte[][] parseIPAddresses(String[] addresses){
		if(addresses==null) return new byte[][]{{}};
		LinkedList<byte[]> output=new LinkedList<byte[]>();
		for(String address: addresses){
			try {
				output.add(stringToIP4Address(address));
			} catch (Exception e){}
		}		
		return output.toArray(new byte[0][0]);
	}
	/**
	 * Wandelt eine als String gegebene IP-Addresse in das von InetAddress akzeptierte byte[]-Format um.
	 * 
	 * @param str
	 * @return die IP-Addresse im akzeptierten byte[] Format
	 * @throws Exception Falls der String nicht dem IP4 Format entspricht
	 */	
	private final static byte[] stringToIP4Address(String str) throws Exception{
		String[] split=str.split(".");
		if(split.length==4){
			byte[] output=new byte[4];
			int element;
			for(int i=0;i<4;i++){
				element=Integer.parseInt(split[i]);
				if(element<0 || element >255)
					throw new Exception("No IP4 Address Format");
				else
					output[i]=(byte)element;				
			}
			return output;
		}
		else
			throw new Exception("No IP4 Address Format");		
	}

}
