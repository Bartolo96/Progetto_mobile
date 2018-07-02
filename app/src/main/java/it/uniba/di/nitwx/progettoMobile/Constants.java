package it.uniba.di.nitwx.progettoMobile;

import java.security.Key;

/**
 * Created by Tonio on 27/06/2018.
 */

public class Constants {
    /*USER KINDS*/
    protected static final int REGISTERD_USER = 1;
    protected static final int FACEBOOK_USER = 2;
    protected static final int GOOGLE_USER = 3;
    /*HTTP_REQUEST HEADERS USSED*/
    protected static final String CONTENT_TYPE="Content-Type";
    protected static final String TOKEN_TYPE="token_type";
    protected static final String TOKEN_TYPE_BEARER="Bearer";

    protected static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    protected static final String AUTHORIZATON_HEADER = "Authorization";
    protected static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
        /*TOKEN*/

    protected static final String AUTH_TOKEN = "access_token";
    protected static final String REFRESH_TOKEN = "refresh_token";
    protected static final String REGISTER_RESPONSE = "register";


        /*URLs*/

    protected static final String URL_PRODUCTS="http://nitwx.000webhostapp.com/api/products";
    protected static final String URL_AUTH_USER="http://nitwx.000webhostapp.com/auth/authenticate_user";
    protected static final String URL_AUTH_THIRD_PARTY_USER="http://nitwx.000webhostapp.com/auth/authenticate_third_party_user";
    protected static  final String URL_ADD_USER="http://nitwx.000webhostapp.com/api/users/add";

    protected static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnvFS6Gw3FuGmekhEHy3FSF03ylSPesIrU1YR7liNPz4XTfilmdxP1dX8s8/Uwq+2KRqmYDzb28jM2ANmtIgsNzg7I5x5HCjAGobhtbFfr2yXD3C33ZfjEt4ENRS8nRpKsSpUKMjx/TVnKnnkbdjgit8PC8vQBoanIYwBWKJWWiwIDAQAB";

}
