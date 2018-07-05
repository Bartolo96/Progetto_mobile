package it.uniba.di.nitwx.progettoMobile;

import java.security.Key;

/**
 * Created by Tonio on 27/06/2018.
 */

public class Constants {

    static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 23;
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 25;


    /*USER KINDS*/
    static final String USER_TYPE = "user_type";
    static final int REGISTERD_USER = 1;
    static final int FACEBOOK_USER = 2;
    static final int GOOGLE_USER = 3;
    /*HTTP_REQUEST HEADERS USSED*/
    static final String CONTENT_TYPE="Content-Type";
    static final String TOKEN_TYPE="token_type";
    static final String TOKEN_TYPE_BEARER="Bearer";

    static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    static final String AUTHORIZATON_HEADER = "Authorization";
    static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
        /*TOKEN*/
    static final String AUTH_TOKEN = "access_token";
    static final String REFRESH_TOKEN = "refresh_token";
    static final String REGISTER_RESPONSE = "register";


        /*URLs*/

    static final String URL_PRODUCTS = "https://nitwx.000webhostapp.com/api/products";
    static final String URL_AUTH_USER = "https://nitwx.000webhostapp.com/auth/authenticate_user";
    static final String URL_AUTH_THIRD_PARTY_USER = "https://nitwx.000webhostapp.com/auth/authenticate_third_party_user";
    static final String URL_ADD_USER = "https://nitwx.000webhostapp.com/api/users/add";
    static final String URL_REFRESH_ACCESS_TOKEN = "https://nitwx.000webhostapp.com/auth/refresh_access_token";
    static final String URL_GEOFENCES = "https://nitwx.000webhostapp.com/api/geofences";


    static final String PACKAGE_NAME = "it.uniba.di.nitwx.progettoMobile";
    static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnvFS6Gw3FuGmekhEHy3FSF03ylSPesIrU1YR7liNPz4XTfilmdxP1dX8s8/Uwq+2KRqmYDzb28jM2ANmtIgsNzg7I5x5HCjAGobhtbFfr2yXD3C33ZfjEt4ENRS8nRpKsSpUKMjx/TVnKnnkbdjgit8PC8vQBoanIYwBWKJWWiwIDAQAB";

}
