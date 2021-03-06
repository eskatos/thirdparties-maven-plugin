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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal artifact
 */
public class ArtifactThirdPartiesMojo
        extends AbstractThirdPartiesMojo
{

    /**
     * @parameter
     * @required
     */
    private String src;
    /**
     * @parameter
     */
    private String md5;
    /**
     * @parameter
     */
    private String sources;
    /**
     * @parameter
     */
    private String sourcesMd5;
    /**
     * @parameter
     */
    private String javadoc;
    /**
     * @parameter
     */
    private String javadocMd5;

    @Override
    public void doExecute()
            throws MojoExecutionException, MojoFailureException
    {
        File mainFile = handle( "main-artifact", src, md5 );
        project.getArtifact().setFile( mainFile );
        getLog().info( "Changed project main artifact to " + mainFile.getAbsolutePath() );

        if ( sources != null ) {
            File sourcesFile = handle( "sources", sources, sourcesMd5 );
            projectHelper.attachArtifact( project, sourcesFile, "sources" );
            getLog().info( "Attached sources: " + sourcesFile.getAbsolutePath() );
        }

        if ( javadoc != null ) {
            File javadocFile = handle( "javadoc", javadoc, javadocMd5 );
            projectHelper.attachArtifact( project, javadocFile, "javadoc" );
            getLog().info( "Attached javadoc: " + javadocFile.getAbsolutePath() );
        }
    }

    private File handle( String ilk, String url, String md5 )
            throws MojoExecutionException
    {
        File file = new File(
                outputDirectory,
                ilk + '-' + project.getArtifactId() + '-' + project.getVersion() + '.' + project.getPackaging() );
        if ( !alreadyDownloaded( file, md5 ) ) {
            downloadFile( url, md5, file );
        }
        return file;
    }

}
