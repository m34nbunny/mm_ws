mm_ws
=====

This library assists with the building of objects from a ksoap envelope.

Instructions:
-------------
To start off let's talk about how this library works. The library scans a given class type for properties decorated
with @DataMember. It then takes those fields and looks inside the envelope mapping fields to objects. 

Step 1.) Name your properties the exact same name as they are returned in your XML response. Look at SoapUI for details of your response.

Step 2.) Decorate your properties with the following annotations. 

	@DataMember


An example class is below
------------------------- 
 
	public class User {
		@DataMember
		public int UserID;
 
		@DataMember
		public String AuthToken;
 
		@DataMember
		public String Email;
 
		@DataMember
		public String FirstName;
 
		@DataMember
		public String LastName;
 
		@DataMember
		public byte[] Password;
 
		@DataMember
		public byte[] Salt;
 
		@DataMember
		public String Username;
	
		public User() {	}
	}


In order to use the library you simply need to call.

	(User)WS.Read(User.class, envelope);
	(int)WS.ReadSimple(int.class, envelope);
	WS.ReadCollection(User.class, envelope);

Where an envelope is a type of SoapSerializationEnvelope.





For any questions, feel free to email me at steven@m-mb.com

Thanks!







