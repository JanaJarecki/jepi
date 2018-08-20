package evaluationbasics.server;

import java.util.LinkedList;
/**
 * Dient zur Unterstuetzung der EvaluationServer-Klasse.
 * Aufgaben:
 * Formatueberpruefung der Eingaben
 * Zusammenfuehren von nur korrekten Addressen
 * 
 * @author roman
 *
 */
public final class IPAddressListBuilder {
	public final static byte[][] parseIPAddresses(Iterable<String> addresses){
		if(addresses==null) return new byte[][]{{}};
		LinkedList<byte[]> output=new LinkedList<byte[]>();
		for(String address: addresses){
			try {
				output.add(stringToIP4Address(address));
			} catch (Exception e){
				System.out.println("Could not parse IP4 address "+address);
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
			}
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
		String[] split = str.split("\\.");
		if(split.length==4){
			byte[] output=new byte[4];
			int element;
			for(int i=0;i<4;i++){
				element=Integer.parseInt(split[i]);
				if(element<0 || element >255)
					throw new Exception("No IP4 Address Format. Value at index "+i+" out of range: "+element);
				else
					output[i]=(byte)element;				
			}
			return output;
		}
		else
			throw new Exception("No IP4 Address Format. Does not consists of 4 parts separated by points.");
	}

}
