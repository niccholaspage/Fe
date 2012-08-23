package org.melonbrew.fe.database.converter;

public class Response {
	private final ResponseType type;
	
	public Response(ResponseType type){
		this.type = type;
	}
	
	public ResponseType getType(){
		return type;
	}
}
