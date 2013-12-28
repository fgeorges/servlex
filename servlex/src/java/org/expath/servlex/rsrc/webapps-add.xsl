<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:web="http://expath.org/ns/webapp"
                exclude-result-prefixes="web"
                version="1.0">

   <xsl:output indent="yes"/>

   <xsl:param name="root" select="/.."/>
   <xsl:param name="pkg"  select="/.."/>

   <xsl:param name="config-name-1"/>
   <xsl:param name="config-value-1"/>
   <xsl:param name="config-name-2"/>
   <xsl:param name="config-value-2"/>
   <xsl:param name="config-name-3"/>
   <xsl:param name="config-value-3"/>
   <xsl:param name="config-name-4"/>
   <xsl:param name="config-value-4"/>
   <xsl:param name="config-name-5"/>
   <xsl:param name="config-value-5"/>
   <xsl:param name="config-name-6"/>
   <xsl:param name="config-value-6"/>
   <xsl:param name="config-name-7"/>
   <xsl:param name="config-value-7"/>
   <xsl:param name="config-name-8"/>
   <xsl:param name="config-value-8"/>
   <xsl:param name="config-name-9"/>
   <xsl:param name="config-value-9"/>
   <xsl:param name="config-name-10"/>
   <xsl:param name="config-value-10"/>
   <xsl:param name="config-name-11"/>
   <xsl:param name="config-value-11"/>
   <xsl:param name="config-name-12"/>
   <xsl:param name="config-value-12"/>
   <xsl:param name="config-name-13"/>
   <xsl:param name="config-value-13"/>
   <xsl:param name="config-name-14"/>
   <xsl:param name="config-value-14"/>
   <xsl:param name="config-name-15"/>
   <xsl:param name="config-value-15"/>
   <xsl:param name="config-name-16"/>
   <xsl:param name="config-value-16"/>
   <xsl:param name="config-name-17"/>
   <xsl:param name="config-value-17"/>
   <xsl:param name="config-name-18"/>
   <xsl:param name="config-value-18"/>
   <xsl:param name="config-name-19"/>
   <xsl:param name="config-value-19"/>
   <xsl:param name="config-name-20"/>
   <xsl:param name="config-value-20"/>

   <xsl:template match="node()" priority="-1">
      <xsl:copy>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="*">
      <xsl:copy>
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="web:webapp">
      <xsl:if test="not(@root = $root)">
         <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
         </xsl:copy>
      </xsl:if>
   </xsl:template>

   <xsl:template match="web:webapps">
      <xsl:copy>
         <xsl:apply-templates select="*"/>
         <webapp xmlns="http://expath.org/ns/webapp" root="{ $root }">
            <package name="{ $pkg }"/>
            <xsl:if test="$config-name-1">
               <config-param name="{ $config-name-1 }" value="{ $config-value-1 }"/>
            </xsl:if>
            <xsl:if test="$config-name-2">
               <config-param name="{ $config-name-2 }" value="{ $config-value-2 }"/>
            </xsl:if>
            <xsl:if test="$config-name-3">
               <config-param name="{ $config-name-3 }" value="{ $config-value-3 }"/>
            </xsl:if>
            <xsl:if test="$config-name-4">
               <config-param name="{ $config-name-4 }" value="{ $config-value-4 }"/>
            </xsl:if>
            <xsl:if test="$config-name-5">
               <config-param name="{ $config-name-5 }" value="{ $config-value-5 }"/>
            </xsl:if>
            <xsl:if test="$config-name-6">
               <config-param name="{ $config-name-6 }" value="{ $config-value-6 }"/>
            </xsl:if>
            <xsl:if test="$config-name-7">
               <config-param name="{ $config-name-7 }" value="{ $config-value-7 }"/>
            </xsl:if>
            <xsl:if test="$config-name-8">
               <config-param name="{ $config-name-8 }" value="{ $config-value-8 }"/>
            </xsl:if>
            <xsl:if test="$config-name-9">
               <config-param name="{ $config-name-9 }" value="{ $config-value-9 }"/>
            </xsl:if>
            <xsl:if test="$config-name-10">
               <config-param name="{ $config-name-10 }" value="{ $config-value-10 }"/>
            </xsl:if>
            <xsl:if test="$config-name-11">
               <config-param name="{ $config-name-11 }" value="{ $config-value-11 }"/>
            </xsl:if>
            <xsl:if test="$config-name-12">
               <config-param name="{ $config-name-12 }" value="{ $config-value-12 }"/>
            </xsl:if>
            <xsl:if test="$config-name-13">
               <config-param name="{ $config-name-13 }" value="{ $config-value-13 }"/>
            </xsl:if>
            <xsl:if test="$config-name-14">
               <config-param name="{ $config-name-14 }" value="{ $config-value-14 }"/>
            </xsl:if>
            <xsl:if test="$config-name-15">
               <config-param name="{ $config-name-15 }" value="{ $config-value-15 }"/>
            </xsl:if>
            <xsl:if test="$config-name-16">
               <config-param name="{ $config-name-16 }" value="{ $config-value-16 }"/>
            </xsl:if>
            <xsl:if test="$config-name-17">
               <config-param name="{ $config-name-17 }" value="{ $config-value-17 }"/>
            </xsl:if>
            <xsl:if test="$config-name-18">
               <config-param name="{ $config-name-18 }" value="{ $config-value-18 }"/>
            </xsl:if>
            <xsl:if test="$config-name-19">
               <config-param name="{ $config-name-19 }" value="{ $config-value-19 }"/>
            </xsl:if>
            <xsl:if test="$config-name-20">
               <config-param name="{ $config-name-20 }" value="{ $config-value-20 }"/>
            </xsl:if>
         </webapp>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
