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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.apache.shiro.crypto.hash.Md5Hash;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * @goal attached
 */
public class ThirdPartiesMojo
        extends AbstractMojo
{
    // Configuration ---------------------------------------------------------------------------------------------------

    /**
     * @parameter default-value="false"
     */
    private boolean skip;
    /**
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    /**
     * @parameter
     */
    private File thirdPartiesFile;
    /**
     * @parameter
     */
    private ThirdParty[] thirdParties;
    // Context ---------------------------------------------------------------------------------------------------------
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    // Components ------------------------------------------------------------------------------------------------------
    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        if ( skip ) {
            getLog().info( "thirdparties-maven-plugin execution is skipped" );
            return;
        }

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
        try {
            Map<ThirdPartyId, File> downloaded = new HashMap<ThirdPartyId, File>();
            for ( Map.Entry<ThirdPartyId, ThirdParty> eachEntry : fullThirdParties.entrySet() ) {
                ThirdPartyId id = eachEntry.getKey();
                ThirdParty thirdParty = eachEntry.getValue();
                File file = new File( outputDirectory, id.getClassifier() + "." + id.getType() );
                if ( file.exists() ) {
                    if ( thirdParty.getMd5() == null ) {
                        getLog().info( "  Will use already present " + file.getName() );
                        downloaded.put( id, file );
                    } else {
                        String localMd5 = new Md5Hash( new FileInputStream( file ) ).toHex();
                        if ( !thirdParty.getMd5().equals( localMd5 ) ) {
                            throw new MojoExecutionException( "Third party [ " + file.getAbsolutePath() + " ] already exists but md5 do not match" );
                        }
                        getLog().info( "  Will use already present " + file.getName() + " [ " + localMd5 + " ]" );
                        downloaded.put( id, file );
                    }
                }
            }
            return downloaded;
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Unable to check downloaded files", ex );
        }
    }

    private Map<ThirdPartyId, File> downloadMissingFiles( Map<ThirdPartyId, ThirdParty> fullThirdParties, Map<ThirdPartyId, File> downloaded )
            throws MojoExecutionException
    {
        try {
            if ( !outputDirectory.exists() ) {
                FileUtils.forceMkdir( outputDirectory );
            }
            for ( Map.Entry<ThirdPartyId, ThirdParty> eachEntry : fullThirdParties.entrySet() ) {
                ThirdPartyId id = eachEntry.getKey();
                if ( downloaded.get( id ) == null ) {
                    URL url = new URL( eachEntry.getValue().getSrc() );
                    File file = new File( outputDirectory, id.getClassifier() + "." + id.getType() );
                    IOUtil.copy( url.openStream(), new FileOutputStream( file ) );
                    downloaded.put( id, file );
                    getLog().info( "  Downloaded " + file.getName() );
                }
            }
            return downloaded;
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Unable to download third party files", ex );
        }
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
