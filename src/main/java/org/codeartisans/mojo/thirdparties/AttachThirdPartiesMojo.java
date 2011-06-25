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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.FileUtils;

/**
 * @goal attached
 */
public class AttachThirdPartiesMojo
        extends AbstractThirdPartiesMojo
{

    // Configuration ---------------------------------------------------------------------------------------------------
    /**
     * @parameter
     */
    private File thirdPartiesFile;
    /**
     * @parameter
     */
    private ThirdParty[] thirdParties;

    @Override
    public void doExecute()
            throws MojoExecutionException, MojoFailureException
    {
        Map<ThirdPartyId, ThirdParty> fullThirdParties = new ThirdPartiesFileLoader( thirdPartiesFile ).load();
        if ( !fullThirdParties.isEmpty() ) {
            getLog().info( "Loaded " + fullThirdParties.size() + " third parties from properties file" );
        }

        fullThirdParties = applyConfiguration( fullThirdParties );
        getLog().info( "Configured a total of " + fullThirdParties.size() + " third parties" );

        Map<ThirdPartyId, File> downloaded = checkDownloaded( fullThirdParties );
        if ( downloaded.isEmpty() ) {
            getLog().info( "Will download " + fullThirdParties.size() + " files" );
        } else {
            getLog().info( "Will download " + ( fullThirdParties.size() - downloaded.size() ) + " files, " + downloaded.size() + " already exists" );
        }

        downloaded = downloadMissingFiles( fullThirdParties, downloaded );
        getLog().info( "Successfully downloaded all third party files in " + outputDirectory.getAbsolutePath() );

        attachArtifacts( downloaded );
        getLog().info( "All third party artifacts attached to the project" );
    }

    private Map<ThirdPartyId, ThirdParty> applyConfiguration( Map<ThirdPartyId, ThirdParty> fullThirdParties )
            throws MojoExecutionException
    {
        if ( thirdParties != null ) {
            for ( ThirdParty eachConfigured : thirdParties ) {
                ThirdPartyId id = eachConfigured.newIdInstance();
                if ( fullThirdParties.get( id ) != null ) {
                    throw new MojoExecutionException( "Third party " + id + " is defined at least two times, cannot continue" );
                }
                fullThirdParties.put( id, eachConfigured );
            }
        }
        return fullThirdParties;
    }

    private Map<ThirdPartyId, File> checkDownloaded( Map<ThirdPartyId, ThirdParty> fullThirdParties )
            throws MojoExecutionException
    {
        Map<ThirdPartyId, File> downloaded = new HashMap<ThirdPartyId, File>();
        for ( Map.Entry<ThirdPartyId, ThirdParty> eachEntry : fullThirdParties.entrySet() ) {
            ThirdPartyId id = eachEntry.getKey();
            ThirdParty thirdParty = eachEntry.getValue();
            File file = new File( outputDirectory, id.getClassifier() + "." + id.getType() );
            if ( alreadyDownloaded( file, thirdParty.getMd5() ) ) {
                downloaded.put( id, file );
            }
        }
        return downloaded;
    }

    private Map<ThirdPartyId, File> downloadMissingFiles( Map<ThirdPartyId, ThirdParty> fullThirdParties, Map<ThirdPartyId, File> downloaded )
            throws MojoExecutionException
    {
        if ( !outputDirectory.exists() ) {
            try {
                FileUtils.forceMkdir( outputDirectory );
            } catch ( IOException ex ) {
                throw new MojoExecutionException( "Unable to create download directory third party files", ex );
            }
        }
        for ( Map.Entry<ThirdPartyId, ThirdParty> eachEntry : fullThirdParties.entrySet() ) {
            ThirdPartyId id = eachEntry.getKey();
            ThirdParty thirdParty = eachEntry.getValue();
            if ( downloaded.get( id ) == null ) {
                File file = new File( outputDirectory, id.getClassifier() + "." + id.getType() );
                downloadFile( thirdParty.getSrc(), thirdParty.getMd5(), file );
                downloaded.put( id, file );
            }
        }
        return downloaded;
    }

    private void attachArtifacts( Map<ThirdPartyId, File> downloaded )
    {
        for ( Map.Entry<ThirdPartyId, File> eachEntry : downloaded.entrySet() ) {
            ThirdPartyId id = eachEntry.getKey();
            File file = eachEntry.getValue();
            projectHelper.attachArtifact( project, id.getType(), id.getClassifier(), file );
        }
    }

}
