/*
// $Id: HttpProxy.java 92 2008-07-17 07:41:10Z lucboudreau $
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2007-2007 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j.driver.xmla.proxy;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.olap4j.driver.xmla.XmlaOlap4jDriver;
import org.olap4j.impl.Base64;

/**
 * Extends the AbstractCachedProxy and serves as 
 * a production ready http communication class. Every SOAP request 
 * sends a POST call to the destination XMLA server and returns 
 * the response as a byte array, conforming to the Proxy interface.
 * 
 * <p>It also takes advantage of the AbstractHttpProxy cookie 
 * managing facilities. All cookies received from the end point 
 * server will be sent back if they are not expired and they also 
 * conform to cookie domain rules.
 * 
 * @author Luc Boudreau and Julian Hyde
 * @version $Id: HttpProxy.java 92 2008-07-17 07:41:10Z lucboudreau $
 */
public class XmlaOlap4jHttpProxy extends XmlaOlap4jAbstractHttpProxy 
{
    /* (non-Javadoc)
     * @see org.olap4j.driver.xmla.proxy.AbstractHttpProxy#getResponse(java.net.URL, java.lang.String)
     */
    public byte[] getResponse(URL url, String request) 
        throws IOException 
    {                
        // Open connection to manipulate the properties
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoOutput(true);
        
        // Set headers
        urlConnection.setRequestProperty(
                "content-type", "text/xml");
        urlConnection.setRequestProperty ( 
                "User-Agent", "Olap4j("
                .concat(XmlaOlap4jDriver.VERSION)
                .concat(")")) ;
        urlConnection.setRequestProperty ( 
                "Accept", "text/xml;q=1" );
        urlConnection.setRequestProperty (
                "Accept-Charset", getEncodingCharsetName()
                .concat(";q=1") ) ;
        
        // Encode credentials for basic authentication
        if (url.getUserInfo() != null) {
            String encoding =
                Base64.encodeBytes(url.getUserInfo().getBytes(), 0);
            urlConnection.setRequestProperty(
                "Authorization", "Basic " + encoding);
        }
        
        // Set correct cookies
        this.useCookies(urlConnection);

        // Send data (i.e. POST). Use same encoding as specified in the
        // header.
        final String encoding = getEncodingCharsetName();
        urlConnection.getOutputStream().write(request.getBytes(encoding));
        
        // Get the response, again assuming default encoding.
        InputStream is = urlConnection.getInputStream();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int count;
        
        while ((count = is.read(buf)) > 0) {
            baos.write(buf, 0, count);
        }
        
        // Save the returned cookies for later use
        this.saveCookies(urlConnection);

        return baos.toByteArray();
        
    }

    /* (non-Javadoc)
     * @see org.olap4j.driver.xmla.proxy.AbstractHttpProxy#getResponseViaSubmit(java.net.URL, java.lang.String)
     */
    public Future<byte[]> getResponseViaSubmit(
        final URL url,
        final String request)
    {
        return XmlaOlap4jDriver.getFuture(this, url, request);
    }

    /* (non-Javadoc)
     * @see org.olap4j.driver.xmla.proxy.Proxy#getEncodingCharsetName()
     */
    public String getEncodingCharsetName() {
        return "UTF-8";
    }

}