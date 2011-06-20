/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codeartisans.mojo.thirdparties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.util.StringUtils;

/**
 * Load third parties definition from a java properties file.
 * 
 * Properties are of the following form: classifier.type.[src|md5]
 * Classifiers cannot contain dots.
 * Types can contain dots.
 */
class ThirdPartiesFileLoader
{

    private final File file;

    ThirdPartiesFileLoader( File file )
    {
        this.file = file;
    }

    Map<ThirdPartyId, ThirdParty> load()
            throws MojoExecutionException
    {
        try {
            Map<ThirdPartyId, ThirdParty> fullThirdParties = new HashMap<ThirdPartyId, ThirdParty>();
            if ( file != null ) {
                Properties props = new Properties();
                props.load( new FileInputStream( file ) );
                for ( Map.Entry<Object, Object> eachEntry : props.entrySet() ) {
                    String key = ( String ) eachEntry.getKey();
                    String value = ( String ) eachEntry.getValue();
                    ThirdPartyId id = parseId( key );
                    ThirdParty eachThirdParty = fullThirdParties.get( id );
                    if ( eachThirdParty == null ) {
                        eachThirdParty = new ThirdParty();
                        eachThirdParty.setClassifier( id.getClassifier() );
                        eachThirdParty.setType( id.getType() );
                        fullThirdParties.put( id, eachThirdParty );
                    }
                    switch ( ThirdPartyProps.valueOf( key.substring( key.lastIndexOf( '.' ) + 1 ) ) ) {
                        case src:
                            eachThirdParty.setSrc( value );
                            break;
                        case md5:
                            eachThirdParty.setMd5( value );
                            break;
                    }
                }
            }
            return fullThirdParties;
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Unable to load Third parties file [ " + file.getAbsolutePath() + " ]", ex );
        }
    }

    private ThirdPartyId parseId( String key )
    {
        String[] split = key.split( "\\." );
        if ( split.length < 3 ) {
            throw new IllegalArgumentException( "Property name [ " + key + " ] cannot be parsed" );
        }
        String classifier = split[0];
        String type = StringUtils.join( Arrays.copyOfRange( split, 1, split.length - 1 ), "." );
        String prop = split[split.length - 1];
        ThirdPartyProps.valueOf( prop );
        return new ThirdPartyId( classifier, type );
    }

    private static enum ThirdPartyProps
    {

        src,
        md5;
    }

}
