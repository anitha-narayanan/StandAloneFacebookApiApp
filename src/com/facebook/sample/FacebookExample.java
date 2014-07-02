package com.facebook.sample;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Insight;
import facebook4j.Page;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;

public class FacebookExample {
	public static String app_id = ""; 
	public static String app_secret="";
	public static final String redirect_uri="http://localhost:10000/";
	public static final String scope = "read_stream";
	public static final int goodResponse = 200;
	public static final String dateSeperator="/";
	public static final String pattern = "MM"+dateSeperator+"dd"+dateSeperator+"yyyy";

	public static void main(String[] args) throws Exception{

		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		String accesstoken = doLogin(bufferRead);

		System.out.println();
		System.out.println();
		Facebook facebook = new FacebookFactory().getInstance();
		
		facebook.setOAuthAppId(app_id, app_secret);
		facebook.setOAuthPermissions(scope);
		facebook.setOAuthAccessToken(new AccessToken(accesstoken, 999999999999999L));

		getPageInformation(bufferRead,facebook);
		
		getOwnersInformation(bufferRead,facebook);
	}

	public static String doLogin(BufferedReader bufferRead) throws IOException{
		System.out.print("Enter your application id: ");

		app_id = "590819784307676";//bufferRead.readLine(); //uncomment HERE

		System.out.print("Enter your application secret: ");
		app_secret = "7a8373278d69a33522e6b455c91e92b4";//bufferRead.readLine();  //uncomment HERE

		System.out.println();
		System.out.println("Step 1 of login: Getting code.");
		String code = getCode();
		System.out.println("\nStep 2 of login: Getting accesstoken.");
		String accesstoken = getAccessToken(code);
		System.out.println("\nLogged in successfully !");

		return accesstoken;
	} 

	public static String  getCode(){
		String authrozationUrl = "https://www.facebook.com/dialog/oauth?" +
				"client_id="+app_id + 
				"&redirect_uri="+URLEncoder.encode(redirect_uri) + 
				"&scope="+scope;		
		String accessToken = null;

		Desktop d=Desktop.getDesktop(); 
		try {
			com.proxy.ProxyServer ps = new com.proxy.ProxyServer(null);
			ps.start();
			d.browse(new URI(authrozationUrl));
			try {
				ps.join();
				accessToken = ps.code;
				System.out.println("getcode: Code: " + ps.code);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return accessToken;
	}

	public static String getAccessToken(String code) throws IOException{

		String url = "https://graph.facebook.com/oauth/access_token?" +
				"client_id=" + app_id + 
				"&redirect_uri=" + URLEncoder.encode(redirect_uri) +
				"&client_secret=" + app_secret +
				"&code=" + code;

		String response = doGet(url);

		String[] returnValues = response.toString().split("&");
		if(returnValues!=null && returnValues.length > 1){
			String[] accessToken = returnValues[0].split("=");
			if(accessToken != null && accessToken.length > 1)
				return accessToken[1];
		}

		//this shouldnt happen
		throw new IOException("Error in getting accessToken");

	}

	public static void getPageInformation(BufferedReader bufferRead, Facebook facebook) throws Exception{
		try {
			System.out.println("\n\nGetting page information");
			System.out.println("===========================");
			
			System.out.println("Enter some text file with public page names");
			String inputFile = bufferRead.readLine();
			BufferedReader input = null;
			try{
				input =  new BufferedReader(new FileReader(inputFile));
			}catch(java.io.FileNotFoundException e){
				throw new Exception("Not a valid file");
			}
			String publicPage = null;
			System.out.println("Enter number of newsfeeds to retreive ");
			String numOfFeeds = bufferRead.readLine();

			int iNumofFeeds = 10;
			try{
				iNumofFeeds = Integer.parseInt(numOfFeeds);
			}catch(NumberFormatException nfe){
				throw new Exception("Enter valid number");
			}

			while((publicPage=input.readLine())!=null){
				Page page = facebook.getPage(publicPage);
				System.out.println("**Number of Likes for "+publicPage+" page: "+page.getLikes()+"**");


				ResponseList<Post> resList = facebook.getFeed(publicPage, new Reading().limit(iNumofFeeds));
				System.out.println("Printing newsFeeds of "+publicPage);
				System.out.println("===========================");
				doPrintPost(resList);
			}
			
			
		} catch (FacebookException e) {
			e.printStackTrace();
		}

	}
	
	public static void getOwnersInformation(BufferedReader bufferRead, Facebook facebook) throws Exception{
		System.out.println("\n\nGetting Owners information");
		System.out.println("===========================");
		Reading rg = new Reading();
		System.out.print("Enter start date in "+pattern+" format: ");
		String startDate = bufferRead.readLine();
		SimpleDateFormat ft = new SimpleDateFormat (pattern);  
		rg.since(parseDate(startDate));
		System.out.print("\nEnter end date in "+pattern+" format: ");
		startDate = bufferRead.readLine();
		
		rg.until(parseDate(startDate));
		ResponseList<Post> resList = facebook.getFeed(rg);
		Paging<Post> paging1 = resList.getPaging();
		System.out.println("Printing your newsFeeds");
		System.out.println("===========================");
		doPrintPost(resList);		
	}
	
	public static void doPrintPost(ResponseList<Post> resList){
		for(Post is : resList){
			//System.out.println(is);
			System.out.println("Name: "+is.getName()); //either Timeline Photos / Pages / Comment
			System.out.println("Message: " + is.getMessage()); //message user posted along with the post element like link, photo, page
			System.out.println("Likes: "+is.getLikes().size()); //number of likes
			System.out.println("share count: "  + is.getSharesCount()); //number of ppl shared this post
			System.out.println("story: " + is.getStory()); //a gist of the post
			System.out.println("type: " + is.getStory()); //link / photo / status
			System.out.println("comments: " );
			for(Comment cm :  is.getComments()){
				//prints comments, number of likes for the comment and who commented.
				System.out.println("message: " + cm.getMessage() + " Likes: " + cm.getLikeCount() + " From: " + cm.getFrom().getName());
			}
			System.out.println();
		}		
	}
		

	public static String doGet(String url) throws IOException{
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		int responseCode = con.getResponseCode();
		if(responseCode != goodResponse)
			throw new IOException("Error in connecting url "+url);
		System.out.println("Sending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println("response for accesstoken http request: "+response.toString());
		return response.toString();

	}
	
	
	
	public static Date parseDate(String strDate) throws Exception
	{  	
		strDate = strDate.trim();
		String errorMsg = "";// Date must have the format -> " + pattern+".";
		try
		{
					
			errorMsg = " Date must have the format -> " + pattern+".";
			
			String [] dateParse = strDate.split("[" + dateSeperator + ":]|\\s+", 0);
			if (dateParse.length == 3){
				int mm = Integer.parseInt(dateParse[0]);//month value extracted
				if(mm <= 0 || mm >12 )
					throw new Exception(" Month exceeds the range. It must be between 1 and 12.");
				
				int dd = Integer.parseInt(dateParse[1]);//date value extracted
				if(dd <= 0 || dd >31 )
					throw new Exception(" Date exceeds the range. It must be between 1 and 31.");
				
				int yyyy = Integer.parseInt(dateParse[2]);//year value extracted
				if(yyyy < 1970 )
					throw new Exception(" Year should be greater than or equal to 1970.");
				
				if (dd > 28){
					if (dd == 31 && (mm == 2 || mm == 4 || mm == 6 || mm == 9 || mm == 11))//date 31 is eliminated
						throw new Exception(" Day 31 not valid for given month.");		   //for all the months					
					else if (mm == 2){
						if (dd == 30)//30 in feb in not valid
							throw new Exception(" Day 30 is not valid for Feb.");
						else if(yyyy % 4 != 0)//for 29 need to check leap year.
							throw new Exception(" Day 29 is not valid for Feb in " + yyyy + ".");
					}
				}
								
			}
			else
				throw new Exception ("Illegal date format :" + errorMsg);
						
			DateFormat dateformat = new SimpleDateFormat(pattern);
			Date dateObj = dateformat.parse(strDate);
			return dateObj;

		}
		catch (NumberFormatException nfe)
		{
			System.out.println(nfe.getMessage());
			throw new Exception("Illegal date format :"+ errorMsg +" " + nfe.getMessage() );   
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			throw new Exception(e.getMessage() );   
		}
		
	}


}
