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

class ThirdPartyId
{

    private final String classifier;
    private final String type;

    public ThirdPartyId( String classifier, String type )
    {
        this.classifier = classifier;
        this.type = type;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return "{ classifier=" + classifier + " type=" + type + " }";
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final ThirdPartyId other = ( ThirdPartyId ) obj;
        if ( ( this.classifier == null ) ? ( other.classifier != null ) : !this.classifier.equals( other.classifier ) ) {
            return false;
        }
        if ( ( this.type == null ) ? ( other.type != null ) : !this.type.equals( other.type ) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79 * hash + ( this.classifier != null ? this.classifier.hashCode() : 0 );
        hash = 79 * hash + ( this.type != null ? this.type.hashCode() : 0 );
        return hash;
    }

}
