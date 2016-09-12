package evaluationbasics.Server;

import evaluationbasics.Exceptions.DummyExceptionHandler;

/**
 * Die Klasse dient als Timeoutmanager fuer einen EvaluationRequest-Thread.
 * Sobald dieser laenger als die angegebene Zeit benoetigt, wird der Prozess mit Gewalt abgewuergt.
 * 
 * 
 * @author Roman Bange
 *
 */
public class StudentTimeoutCounter extends Thread{
	
	//Definiert die Timeoutzeit
	private final static int STUDENTTIMEOUT= EvaluationServer.STUDENTTIMEOUT;
	
	private final EvaluationRequest STUDENT;
	private boolean stop=false;
	
	public StudentTimeoutCounter(EvaluationRequest pStudent){
		this.setUncaughtExceptionHandler(new DummyExceptionHandler());
		STUDENT=pStudent;			
	}
	
	@SuppressWarnings("deprecation")
	public void run(){
		long start=System.currentTimeMillis();
		while(((start+StudentTimeoutCounter.STUDENTTIMEOUT) > System.currentTimeMillis()) && !stop){
			try {
				sleep(StudentTimeoutCounter.STUDENTTIMEOUT/20);
			} catch (InterruptedException e) {}
		}
		STUDENT.kill();
		//wuergt den Studentprozess ab falls dieser in einer Dauerschleife ist
		try {
			sleep(2000);
		} catch (InterruptedException e) {}
		if(STUDENT.isAlive())
			/*
			 * Der folgende Methodenaufruf ist deprecated, aufgrund dessen das es zu unvorhergesehen Komplikationen mit anderen Threads kommen kann.(Deadlocks etc)
			 * Aufgrund dessen das ein EvaluationRequest-thread aber weitgehen unabhaengig operiert sollte das keine grossen Probleme bereiten.
			 * 
			 * Mir ist bislang keine Moeglichkeit bekannt eine moegliche Dauerschleife einer
			 * Studentenmethode anders zu unterbrechen.
			 * 
			 * Ob diese Methode auch in Java 8 funktioniert ist bislang nicht getestet!!
			 * Falls eine Alternative gefunden wird ist diese Methode sofort zu ersetzen.
			 */
			STUDENT.stop(); 
		
	}
	/**
	 * Gibt ein Zeichen zur sanften Beendigung des TimeoutCounter
	 */
	public void kill(){
		stop=true;
	}

}
