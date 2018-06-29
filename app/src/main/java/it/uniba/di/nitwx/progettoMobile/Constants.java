package it.uniba.di.nitwx.progettoMobile;

/**
 * Created by Tonio on 27/06/2018.
 */

public class Constants {
    /*USER KINDS*/
    protected static final int REGISTERD_USER = 1;
    protected static final int FACEBOOK_USER = 2;
    protected static final int GOOGLE_USER = 3;
    /*HTTP_REQUEST CONSTANTS*/

    protected static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

        /*TOKEN*/

    protected static final String AUTH_TOKEN = "authtoken";
    protected static final String REFRESH_TOKEN = "refreshtoken";
    protected static final String REGISTER_RESPONSE = "register";


        /*URLs*/

    protected static final String URL_PRODUCTS="http://nitwx.000webhostapp.com/api/products";
    protected static final String URL_AUTH_USER="http://nitwx.000webhostapp.com/auth/authenticate_user";
    protected static final String URL_AUTH_THIRD_PARTY_USER="http://nitwx.000webhostapp.com/auth/authenticate_third_party_user";
    protected static  final String URL_ADD_USER="http://nitwx.000webhostapp.com/api/users/add";
}
