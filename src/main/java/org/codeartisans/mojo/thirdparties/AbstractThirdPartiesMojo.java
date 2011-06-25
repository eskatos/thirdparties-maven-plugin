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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.apache.shiro.crypto.hash.Md5Hash;

import org.codehaus.plexus.util.IOUtil;

public abstract class AbstractThirdPartiesMojo
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
    protected File outputDirectory;
    // Context ---------------------------------------------------------------------------------------------------------
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
    // Components ------------------------------------------------------------------------------------------------------
    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    @Override
    public final void execute()
            throws MojoExecutionException, MojoFailureException
    {
        if ( skip ) {
            getLog().info( "thirdparties-maven-plugin execution is skipped" );
            return;
        }
        doExecute();
    }

    protected abstract void doExecute()
            throws MojoExecutionException, MojoFailureException;

    protected final boolean alreadyDownloaded( File fileCandidate, String md5 )
            throws MojoExecutionException
    {
        try {
            if ( !fileCandidate.exists() ) {
                return false;
            }
            if ( md5 == null ) {
                getLog().info( "  Will use already present " + fileCandidate.getName() );
                return true;
            } else {
                String localMd5 = new Md5Hash( new FileInputStream( fileCandidate ) ).toHex();
                if ( !md5.equals( localMd5 ) ) {
                    throw new MojoExecutionException( "Third party " + fileCandidate.getName() + " already exists but md5 do not match" );
                }
                getLog().info( "  Will use already present " + fileCandidate.getName() + " [ " + localMd5 + " ]" );
                return true;
            }
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Unable to check downloaded file", ex );
        }
    }

    protected final void downloadFile( String src, String md5, File file )
            throws MojoExecutionException
    {
        try {
            URL url = new URL( src );
            IOUtil.copy( url.openStream(), new FileOutputStream( file ) );
            if ( md5 == null ) {
                getLog().info( "  Downloaded " + file.getName() );
            } else {
                String localMd5 = new Md5Hash( new FileInputStream( file ) ).toHex();
                if ( !md5.equals( localMd5 ) ) {
                    throw new MojoExecutionException( "Third party " + file.getName() + " downloaded but md5 do not match" );
                }
                getLog().info( "  Downloaded " + file.getName() + " [ " + localMd5 + " ]" );
            }
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Unable to download third party file from " + src, ex );
        }
    }

}
