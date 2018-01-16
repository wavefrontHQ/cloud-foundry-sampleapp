package com.vmware.wavefront.util;
import org.slf4j.Logger;

public class ASCIIArtsLogger
{
	public static final String[] logo = 
		{
"                          __                 _   "
,"                         / _|               | |  "
,"__      ____ ___   _____| |_ _ __ ___  _ __ | |_" 
,"\\ \\ /\\ / / _` \\ \\ / / _ \\  _| '__/ _ \\| '_ \\| __|"
," \\ V  V / (_| |\\ V /  __/ | | | | (_) | | | | |_ "
,"  \\_/\\_/ \\__,_| \\_/ \\___|_| |_|  \\___/|_| |_|\\__|"
		};
	
	public static final String[] error =
		{
				"  ___ _ __ _ __ ___  _ __ "
			,	" / _ \\ '__| '__/ _ \\| '__|"
			,	"|  __/ |  | | | (_) | |   "
			,	" \\___|_|  |_|  \\___/|_|   "
		};
			
	public static void logWavefront(Logger logger)
	{	
		for(String line : logo)
		{
			logger.info(line);
		}
	}
	
	public static void logError(Logger logger)
	{
		for(String line : error)
		{
			logger.error(line);
		}
	}
}
