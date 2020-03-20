package respuestas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import com.google.common.io.ByteStreams;

import servidor.ProcesadorXML;

public class RespuestaGET implements HttpRespuesta 
{
	// Estructura de una respuesta https://www.tutorialspoint.com/http/http_responses.htm
	
	private final String PATH_RAIZ = "src/main/resources/httpdoc";
	private ProcesadorXML procesadorXML;
	
	public RespuestaGET() 
	{
		procesadorXML = new ProcesadorXML();
	}
	
	@Override
	public void procesarSolicitud(String mensajeSolicitud, OutputStream salida) 
	{
		try 
		{
			procesarGET(mensajeSolicitud, salida);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void procesarGET(String mensajeSolicitud, OutputStream salida) throws IOException
	{
		File archivoSolicitado = null;
		// mensajeSolicitud tine / de primero
		String path = PATH_RAIZ + mensajeSolicitud;

		// Si la solicitud se hace sin especificar archivo, se supone un index.html
		if(mensajeSolicitud.length() > 0 && mensajeSolicitud.charAt(mensajeSolicitud.length() - 1) == '/' )
		{			
			// Obtener index.html
			path = path.concat("index.html");
		}

		// Obtener del archivo de acuerdo al path de parametro
		archivoSolicitado = new File(path);
		
		
		if(archivoSolicitado.exists())
		{
			salida.write(convertirBytes("HTTP/1.1 200 OK\r\n"));
			
			salida.write(convertirBytes("Date: " + Instant.now().toString()   + "GMT\r\n")); // FALTA FORMATO
			salida.write(convertirBytes("Server: MiServidor/1.0\r\n"));
			salida.write(convertirBytes("Content-Length: " + archivoSolicitado.length() +"\r\n"));
			
			try 
			{
				// Extraer el dominio y obtener el tipo de acuerdo a web.xml
				salida.write(convertirBytes("Content-Type: " + procesadorXML.obtenerTipo(obtenerExtension(archivoSolicitado)) + "\r\n"));
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			salida.write(convertirBytes("\r\n")); // Listo los headers, lo siguiente es el mensaje como tal
			
			responderArchivo(salida, archivoSolicitado);
			
			// El que llama al metodo se tiene que encargar de hacer flush y cerrar.
		}
		else
		{
			// Error
		}
	}
	
	private byte[] convertirBytes(String string)
	{
		return string.getBytes();
	}
	
	private void responderArchivo(OutputStream salida, File archivo) throws IOException
	{
		InputStream entrada = new FileInputStream(archivo);
		
		ByteStreams.copy(entrada, salida);
	}
	
	private String obtenerExtension(File archivo)
	{
		String fileName = archivo.getName();
		
		// Si hay . y si el nombre no termina con .
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        {
            return fileName.substring(fileName.lastIndexOf(".")+1);
        }
        else 
        {
        	return "";
        }
	}

}
