<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

    <xsl:template mode="css" match="/" priority="1">

        <xsl:call-template name="geoCssHeader"/>
        <xsl:call-template name="ext-ux-css"/>

        <!-- AIMS intranet CSS
        -->
		<link href="http://www.aims.gov.au/aims-theme/images/favicon.ico,favicon.ico" rel="Shortcut Icon"/>
        <link rel="stylesheet" type="text/css" href="http://www.aims.gov.au/aims-theme/css/custom.css"/>
        <link rel="stylesheet" type="text/css" href="{/root/gui/url}/aims_internet_overrides.css"/>


        <!-- AIMS Intranet scripts and CSS
        <script src="http://intranet.aims.gov.au/flatui-theme/js/jquery-1.8.3.min.js" type="text/javascript"></script>
        <script src="http://intranet.aims.gov.au/flatui-theme/js/bootstrap.js" type="text/javascript"></script>
        <link href="http://intranet.aims.gov.au/flatui-theme/css/main.css" rel="stylesheet" type="text/css"/>
        <link href="http://intranet.aims.gov.au/flatui-theme/css/bootstrap.css" rel="stylesheet" type="text/css"/>
        <link href="http://intranet.aims.gov.au/flatui-theme/css/flat-ui.css" rel="stylesheet" type="text/css"/>
        <link href="http://intranet.aims.gov.au/flatui-theme/css/custom.css" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="{/root/gui/url}/aims_intranet_overrides.css"/>
        -->
    </xsl:template>

    <xsl:template name="custom_banner">
        <tr class="aimsbannertitle">
            <td class="aimsbannertitle" colspan="3">
                <xsl:call-template name="aims_internet_banner_20140701"/>
                <!--
                <xsl:call-template name="aims_intranet_banner"/>
                -->
            </td>
        </tr>
    </xsl:template>

    <xsl:template name="aims_footer">
        <xsl:call-template name="aims_internet_footer"/>
        <!--
        <xsl:call-template name="aims_intranet_footer"/>
        -->
    </xsl:template>

    <xsl:template name="aims_intranet_banner">
        <xsl:if test="string(/root/gui/session/userId)!=''">
            <div class="dockbar" id="dockbar">
                <ul class="aui-toolbar user-toolbar">

                    <li class="aui-toolbar-separator">
                        <span></span>
                    </li>
                    <li>
                        <span>
                            <a href="http://intranet.aims.gov.au/help">Help</a>
                        </span>
                    </li>
                    <li class="aui-toolbar-separator">
                        <span></span>
                    </li>
                    <li>
                        <span>
                            <a href="http://intranet.aims.gov.au/web/non-public">Non Public</a>
                        </span>
                    </li>
                    <li class="aui-toolbar-separator">
                        <span></span>
                    </li>

                    <li class="user-avatar " id="_145_userAvatar">
                        <xsl:if test="string(/root/gui/session/userId)!=''">
                            <xsl:variable name="fname" select="/root/gui/session/name"/>
                            <xsl:variable name="lname" select="/root/gui/session/surname"/>
                            <xsl:variable name="uname" select="/root/gui/session/username"/>
                            <span class="user-links ">
                                <a class="user-portrait"
                                   href="http://people.aims.gov.au:8080/peoplefinder/faces/jsp/SearchGUI.xhtml?target=user"
                                   style="z-index: 100000;">
                                    <img alt="{$fname} {$lname}"
                                         src="http://people.aims.gov.au:8080/peoplefinder/rest/active/image/username/{$uname}"/>
                                </a>
                                <a class="user-fullname"
                                   href="http://people.aims.gov.au:8080/peoplefinder/faces/jsp/SearchGUI.xhtml?target=user">
                                    <xsl:value-of select="/root/gui/session/name"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="/root/gui/session/surname"/>
                                </a>
                                <span class="sign-out">(<a href="/c/portal/logout">Sign Out</a>)
                                </span>
                            </span>
                        </xsl:if>
                    </li>
                </ul>
            </div>
        </xsl:if>
        <div id="header">
            <header id="banner" role="banner">
                <div class="navbar navbar-inverse">
                    <div class="navbar-inner">
                        <div class="container">
                            <button class="btn btn-navbar" data-target="#nav-collapse-03" data-toggle="collapse"
                                    type="button"></button>
                            <div class="nav-collapse collapse" id="_t12">
                                <ul class="nav">
                                    <li>
                                        <a class="brand" href="http://intranet.aims.gov.au"
                                           style="border-right:0px">Home
                                        </a>
                                    </li>
                                    <li>
                                        <a href="http://intranet.aims.gov.au/services">
                                            <span>Services</span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="http://intranet.aims.gov.au/research">
                                            <span>Research</span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="http://people.aims.gov.au:8080/peoplefinder/faces/jsp/SearchGUI.xhtml">
                                            <span>People</span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="http://intranet.aims.gov.au/policies-and-procedures">
                                            <span>Policies</span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="http://intranet.aims.gov.au/forms">
                                            <span>Forms</span>
                                        </a>
                                    </li>
                                    <xsl:choose>
                                        <xsl:when test="string(/root/gui/session/userId)!=''">
                                            <li>
                                                <a href="http://intranet.aims.gov.au/me">
                                                    <span>Me</span>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="http://intranet.aims.gov.au/my-tasks">
                                                    <span class="fui-mail-3"></span>
                                                    <span class="hidden-desktop">View Tasks</span>
                                                    <span class="navbar-new" id="my-tasks-result">0</span>
                                                </a>
                                            </li>
                                            <li>
                                                <form action="http://search.aims.gov.au/search"
                                                      class="navbar-search form-search pull-left" method="get">

                                                    <div class="input-append">
                                                        <input name="proxyreload" type="hidden" value="1"/>

                                                        <input name="site" type="hidden" value="All_collections"/>
                                                        <input name="output" type="hidden" value="xml_no_dtd"/>
                                                        <input name="client" type="hidden" value="all-content_public_frontend"/>
                                                        <input name="proxystylesheet" type="hidden"
                                                               value="all-content_public_frontend"/>
                                                        <input name="access" type="hidden" value="p"/>
                                                        <input class="search-query span3" maxlength="255" name="q"
                                                               onclick="this.value=''" placeholder="Search" type="text"/>
                                                        <button class="btn btn-large" type="submit">
                                                            <i class="fui-search"></i>
                                                        </button>
                                                    </div>
                                                </form>
                                            </li>
                                            <li>
                                                <a href="http://intranet.aims.gov.au/c/portal/logout" id="sign-out" rel="nofollow"
                                                   style="border-left:0px;">Sign Out
                                                </a>
                                            </li>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <li>
                                                <form action="http://search.aims.gov.au/search"
                                                      class="navbar-search form-search pull-left" method="get">
                                                    <!-- Clear the GSA cache during development. -->
                                                    <div class="input-append">
                                                        <input name="proxyreload" type="hidden" value="1"/>
                                                        <!-- Standard parameters. -->
                                                        <input name="site" type="hidden"
                                                               value="All_collections"/>
                                                        <input
                                                                name="output" type="hidden" value="xml_no_dtd"/>
                                                        <input
                                                                name="client" type="hidden" value="all-content_public_frontend"/>
                                                        <input
                                                                name="proxystylesheet" type="hidden"
                                                                value="all-content_public_frontend"/>
                                                        <input name="access"
                                                               type="hidden" value="p"/>
                                                        <input maxlength="255" name="q"
                                                               onclick="this.value=''" class="search-query span3" type="text"
                                                               placeholder="Search"/>
                                                        <button class="btn btn-large" type="submit">
                                                            <i class="fui-search"></i>
                                                        </button>
                                                    </div>
                                                </form>
                                            </li>
                                            <li>
                                                <xsl:variable name="casparams">
                                                    <xsl:apply-templates mode="casParams" select="root/request/*"></xsl:apply-templates>
                                                </xsl:variable>
                                                <a class="banner" href="{/root/gui/locService}/{/root/gui/reqService}?casLogin{$casparams}" id="sign-in" rel="nofollow"
                                                   style="border-left:0px;">Sign In
                                                </a>
                                            </li>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            <nav class="site-breadcrumbs" id="breadcrumbs">
                <ul class="links breadcrumb">
                    <li class="first">
                        <a href="http://intranet.aims.gov.au" id="_t18" name="_t18">
                            <span
                                    id="_t19">AIMSCAPE
                            </span>
                        </a>
                    </li>
                    <li class="last">
                        <span id="appname">AIMS Internal Data Catalogue</span>
                    </li>
                </ul>
            </nav>
        </div>
    </xsl:template>

    <!-- Intranet footer template -->
    <xsl:template name="aims_intranet_footer">
        <div class="bottom-menu bottom-menu-inverse">
            <div class="container container-override">
                <div class="row">
                    <div class="span2 brand">
                        <a href="http://data.aims.gov.au">
                            <img
                                    src="http://intranet.aims.gov.au/intranet-theme/images/text.png"/>
                        </a>
                    </div>
                    <div class="span2 width150">
                        <h5 class="title">Services</h5>
                        <ul class="">
                            <li>
                                <a href="http://intranet.aims.gov.au/health-and-safety">Health and Safety</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/human-resources">Human Resources</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/purchasing">Purchasing</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/travel">Travel</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/finance">Finance</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/transport">Transport</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/other-services">More Services</a>
                            </li>
                        </ul>
                    </div>
                    <div class="span2 width150">
                        <h5 class="title">Research</h5>
                        <ul class="">
                            <li>
                                <a href="http://intranet.aims.gov.au/field-trips">Field Trips</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/information-services">Information Services</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/research-data">Research Data</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/research-management">Research Management</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/using-the-labs">Using The Labs</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/aqis">AQIS</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/research">More Research</a>
                            </li>
                        </ul>
                    </div>
                    <div class="span2 width150">
                        <h5 class="title">Quick Links</h5>
                        <ul class="">
                            <li>
                                <a href="http://intranet.aims.gov.au/late-car-booking">Late Car</a>
                            </li>
                            <li>
                                <a href="http://delegation.aims.gov.au:8080/delegation/faces/jsf/listRoles.xhtml">Delegation</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/away-system">Away</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/logreq">Logreq</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/visitors">Visitors</a>
                            </li>
                            <li>
                                <a href="http://helpdesk.aims.gov.au:8001/HomePage.do">HELP Desk</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/forms">More</a>
                            </li>
                        </ul>
                    </div>
                    <div class="span2 width200">
                        <h5 class="title">Contact</h5>
                        <ul class="">
                            <li>
                                <a href="mailto:reception@aims.gov.au">Reception - 4444</a>
                            </li>
                            <li>
                                <a href="http://intranet.aims.gov.au/hse-first-aid">First AID and Fire - 99</a>
                            </li>
                            <li>Fire, Ambulance - (0)000</li>
                            <li>Security Guard - 041 7609512</li>
                            <li>
                                <a href="mailto:safety@aims.gov.au">Safety Officer - 4473</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="aims_internet_banner_20140701">
		<header class="main-header">
			<a href="/">
				<img alt="Australian Institute of Marine Science" src="http://www.aims.gov.au/aims-theme/images/Banner_left.jpg"/>
			</a>
			<!-- google search -->
			<form action="/web/guest/search" class="cse-search-form">
				<input class="search-input" name="q" placeholder="Search..." type="search"/>
			</form>
			<!-- navigation -->
			<nav class="sort-pages modify-pages" id="navigation" role="navigation">
				<ul aria-label="Site Pages" role="menubar">
					<li class="" id="layout_706" role="presentation">
						<a aria-labelledby="layout_706"
						   href="http://www.aims.gov.au/welcome"
						   role="menuitem"><span> Home</span></a>
					</li>
					<li class="" id="layout_6" role="presentation">
						<a aria-labelledby="layout_6" href="http://www.aims.gov.au/docs/about/about.html"
						   aria-haspopup="true" role="menuitem"><span> About AIMS</span>
						</a>
						<ul class="child-menu" role="menu">
							<li class="false" id="layout_16" role="presentation">
								<a aria-labelledby="layout_16"
								   href="http://www.aims.gov.au/docs/about/corporate/corporate-profile.html"
								   role="menuitem">Corporate Profile</a>
							</li>
							<li class="false" id="layout_7" role="presentation">
								<a aria-labelledby="layout_7"
								   href="http://www.aims.gov.au/docs/about/business/doing-business.html"
								   role="menuitem">Business</a>
							</li>
							<li class="false" id="layout_21" role="presentation">
								<a aria-labelledby="layout_21"
								   href="http://www.aims.gov.au/docs/about/facilities/facilities.html"
								   role="menuitem">Facilities</a>
							</li>
							<li class="false" id="layout_27" role="presentation">
								<a aria-labelledby="layout_27"
								   href="http://www.aims.gov.au/docs/about/partnerships/partnerships.html"
								   role="menuitem">Partnerships</a>
							</li>
							<li class="false" id="layout_39" role="presentation">
								<a aria-labelledby="layout_39"
								   href="http://www.aims.gov.au/docs/about/visiting/visiting-aims.html"
								   role="menuitem">Visiting AIMS</a>
							</li>
							<li class="false" id="layout_41" role="presentation">
								<a aria-labelledby="layout_41"
								   href="http://www.aims.gov.au/docs/about/working/working-at-aims.html"
								   role="menuitem">Working at AIMS</a>
							</li>
							<li class="false" id="layout_10" role="presentation">
								<a aria-labelledby="layout_10"
								   href="http://www.aims.gov.au/docs/about/contacts.html"
								   role="menuitem">Contact Us</a>
							</li>
							<li class="false" id="layout_658" role="presentation">
								<a aria-labelledby="layout_658"
								   href="http://www.aims.gov.au/ips"
								   role="menuitem">Access to AIMS Info</a>
							</li>
						</ul>
					</li>
					<li class="" id="layout_273" role="presentation">
						<a aria-labelledby="layout_273"
						   href="http://www.aims.gov.au/docs/research/research.html"
						   aria-haspopup="true" role="menuitem"><span> Research</span>
						</a>
						<ul class="child-menu" role="menu">
							<li class="false" id="layout_274" role="presentation">
								<a aria-labelledby="layout_274"
								   href="http://www.aims.gov.au/docs/research/biodiversity-ecology/biodiversity-ecology.html"
								   role="menuitem">Biodiversity &amp; Ecology</a>
							</li>
							<li class="false" id="layout_295" role="presentation">
								<a aria-labelledby="layout_295"
								   href="http://www.aims.gov.au/docs/research/climate-change/climate-change.html"
								   role="menuitem">Climate Change</a>
							</li>
							<li class="false" id="layout_309" role="presentation">
								<a aria-labelledby="layout_309"
								   href="http://www.aims.gov.au/docs/research/ecosystem-health/ecosystem-health.html"
								   role="menuitem">Ecosystem Health</a>
							</li>
							<li class="false" id="layout_310" role="presentation">
								<a aria-labelledby="layout_310"
								   href="http://www.aims.gov.au/docs/research/marine-microbes/microbes/microbes.html"
								   role="menuitem">Marine Microbes</a>
							</li>
							<li class="false" id="layout_319" role="presentation">
								<a aria-labelledby="layout_319"
								   href="http://www.aims.gov.au/docs/research/monitoring/monitoring.html"
								   role="menuitem">Monitoring</a>
							</li>
							<li class="false" id="layout_423" role="presentation">
								<a aria-labelledby="layout_423"
								   href="http://www.aims.gov.au/docs/research/sustainable-use/sustainable-use.html"
								   role="menuitem">Sustainable Use</a>
							</li>
							<li class="false" id="layout_450" role="presentation">
								<a aria-labelledby="layout_450"
								   href="http://www.aims.gov.au/docs/research/water-quality/water-quality.html"
								   role="menuitem">Water Quality</a>
							</li>
							<li class="false" id="layout_341" role="presentation">
								<a aria-labelledby="layout_341"
								   href="http://www.aims.gov.au/docs/research/research-activities/research-activities.html"
								   role="menuitem">Activities</a>
							</li>
							<li class="false" id="layout_349" role="presentation">
								<a aria-labelledby="layout_349"
								   href="http://www.aims.gov.au/docs/research/research-capabilities/capability-statement-01.html"
								   role="menuitem">Capabilities</a>
							</li>
							<li class="false" id="layout_421" role="presentation">
								<a aria-labelledby="layout_421"
								   href="http://www.aims.gov.au/docs/research/research-links/research-links.html"
								   role="menuitem">Links</a>
							</li>
							<li class="false" id="layout_497" role="presentation">
								<a aria-labelledby="layout_497"
								   href="http://data.aims.gov.au/staffcv/"
								   role="menuitem">Staff</a>
							</li>
						</ul>
					</li>
					<li class="selected" id="layout_60" aria-selected="true" role="presentation">
						<a aria-labelledby="layout_60" href="http://www.aims.gov.au/docs/data/data.html"
						   role="menuitem"><span> Data</span></a>
					</li>
					<li class="" id="layout_74" role="presentation">
						<a aria-labelledby="layout_74"
						   href="http://www.aims.gov.au/docs/media/media.html"
						   aria-haspopup="true" role="menuitem"><span> Media</span>
						</a>
						<ul class="child-menu" role="menu">
							<li class="false" id="layout_513" role="presentation">
								<a aria-labelledby="layout_513"
								   href="http://www.aims.gov.au/docs/media/latest-releases"
								   role="menuitem">Latest releases</a>
							</li>
							<li class="false" id="layout_75" role="presentation">
								<a aria-labelledby="layout_75"
								   href="http://www.aims.gov.au/docs/media/backgrounders.html"
								   role="menuitem">Marine Backgrounders</a>
							</li>
							<li class="false" id="layout_76" role="presentation">
								<a aria-labelledby="layout_76"
								   href="http://www.aims.gov.au/docs/media/email-subscriptions.html"
								   role="menuitem">Email Subscriptions</a>
							</li>
							<li class="false" id="layout_79" role="presentation">
								<a aria-labelledby="layout_79"
								   href="http://www.aims.gov.au/docs/media/media-archives.html"
								   role="menuitem">Release Archives</a>
							</li>
							<li class="false" id="layout_80" role="presentation">
								<a aria-labelledby="layout_80"
								   href="http://www.aims.gov.au/docs/media/media-enquires.html"
								   role="menuitem">Media Enquiries</a>
							</li>
						</ul>
					</li>
					<li class="" id="layout_259" role="presentation">
						<a aria-labelledby="layout_259"
						   href="http://www.aims.gov.au/publications.html"
						   aria-haspopup="true" role="menuitem"><span> Publications</span>
						</a>
						<ul class="child-menu" role="menu">
							<li class="false" id="layout_264" role="presentation">
								<a aria-labelledby="layout_264"
								   href="http://www.aims.gov.au/docs/publications/foi-01-statement.html"
								   role="menuitem">Freedom of Information</a>
							</li>
							<li class="false" id="layout_266" role="presentation">
								<a aria-labelledby="layout_266"
								   href="http://www.aims.gov.au/docs/publications/library-catalogue.html"
								   role="menuitem">Library Catalogue</a>
							</li>
						</ul>
					</li>
				</ul>
			</nav>
		</header>
    </xsl:template>

    <xsl:template name="aims_internet_banner">
        <!--
        <div class="wrapper">
        </div>
        -->
            <!-- header -->
            <header>
                <a href="/">
                    <img alt="Australian Institute of Marine Science"
                         src="http://www.aims.gov.au/aims-theme/images/Banner_left.jpg"/>
                </a>
                <!-- google search -->
                <form action="/web/guest/search" class="cse-search-form">
                    <input class="search-input" name="q"
                           placeholder="Search..." type="search"/>
                </form>
                <!-- navigation -->
                <nav class="sort-pages modify-pages" id="navigation">
                    <h1>
                        <span>Navigation</span>
                    </h1>
                    <ul>
                        <li class="selected">
                            <a href="http://www.aims.gov.au/welcome">
                                <span>Home</span>
                            </a>
                        </li>
                        <li>
                            <a href="http://www.aims.gov.au/docs/about/about.html">
                                <span>About AIMS</span>
                            </a>
                            <ul class="child-menu">
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/corporate/corporate-profile.html">Corporate
                                        Profile
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/business/doing-business.html">Business</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/facilities/facilities.html">Facilities</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/partnerships/partnerships.html">Partnerships</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/visiting/visiting-aims.html">Visiting AIMS</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/working/working-at-aims.html">Working at AIMS</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/about/contacts.html">Contact Us</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/ips">Access to AIMS Info</a>
                                </li>
                            </ul>
                        </li>
                        <li>
                            <a href="http://www.aims.gov.au/docs/research/research.html">
                                <span>Research</span>
                            </a>
                            <ul class="child-menu">
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/biodiversity-ecology/biodiversity-ecology.html">Biodiversity
                                        &amp; Ecology
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/climate-change/climate-change.html">Climate
                                        Change
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/ecosystem-health/ecosystem-health.html">Ecosystem
                                        Health
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/marine-microbes/microbes/microbes.html">Marine
                                        Microbes
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/monitoring/monitoring.html">Monitoring</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/sustainable-use/sustainable-use.html">Sustainable
                                        Use
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/water-quality/water-quality.html">Water
                                        Quality
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/research-activities/research-activities.html">Activities</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/research-capabilities/capability-statement-01.html">Capabilities</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/research/research-links/research-links.html">Links</a>
                                </li>
                                <li>
                                    <a href="http://data.aims.gov.au/staffcv/">Staff</a>
                                </li>
                            </ul>
                        </li>
                        <li>
                            <a href="http://www.aims.gov.au/docs/data/data.html">
                                <span>Data</span>
                            </a>
                        </li>
                        <li>
                            <a href="http://www.aims.gov.au/docs/media/media.html">
                                <span>Media</span>
                            </a>
                            <ul class="child-menu">
                                <li>
                                    <a href="http://www.aims.gov.au/docs/media/latest-releases">Latest releases</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/media/backgrounders.html">Marine Backgrounders</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/media/email-subscriptions.html">Email Subscriptions</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/media/media-archives.html">Release Archives</a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/media/media-enquires.html">Media Enquiries</a>
                                </li>
                            </ul>
                        </li>
                        <li>
                            <a href="http://www.aims.gov.au/publications.html">
                                <span>Publications</span>
                            </a>
                            <ul class="child-menu">
                                <li>
                                    <a href="http://www.aims.gov.au/docs/publications/foi-01-statement.html">Freedom of
                                        Information
                                    </a>
                                </li>
                                <li>
                                    <a href="http://www.aims.gov.au/docs/publications/library-catalogue.html">Library
                                        Catalogue
                                    </a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </nav>
            </header>
    </xsl:template>

    <xsl:template name="aims_internet_footer">
            <!-- footer -->
            <footer>
                <div class="footer-main">
                    <div class="footer-links">
                        <ul>
                            <li><a href="/">Home</a>|
                            </li>
                            <li><a href="/docs/about/about.html">About AIMS</a>|
                            </li>
                            <li><a href="/docs/research/research.html">Research</a>|
                            </li>
                            <li><a href="/docs/data/data.html">Online data</a>|
                            </li>
                            <li><a href="/docs/publications/publications.html">Publications</a>|
                            </li>
                            <li><a href="/docs/media/media.html">Media</a>|
                            </li>
                            <li><a href="/docs/about/site-index.html">Site Index</a>|
                            </li>
                            <li>
                                <a href="/docs/about/contacts.html">Contact Us</a>
                            </li>
                        </ul>
                        <ul>
                            <li><a href="/docs/cc-attribution.html">Attributing AIMS</a>|
                            </li>
                            <li><a href="/docs/cc-copyright.html">Copyright Notice</a>|
                            </li>
                            <li><a href="/docs/disclaimer.html">Disclaimer</a>|
                            </li>
                            <li>
                                <a href="/docs/privacy-policy.html">Privacy Policy</a>
                            </li>
                        </ul>
                        &#169; 1996-2014 Australian Institute of Marine Science
                        <a href="http://creativecommons.org/licenses/by/3.0/au/deed.en">
                            <img src="http://www.aims.gov.au/aims-theme/images/cc.png" alt="Creative Commons 3.0"/>
                        </a>
                    </div>
                    <div class="footer-social">
                        <ul>
                            <li>
                                <a href="http://www.aims.gov.au/rss/aims-rssfeed.xml">
                                    <img src="http://www.aims.gov.au/aims-theme/images/rss.png" width="24" height="24"
                                         alt="AIMS RSS feed"/>
                                </a>
                            </li>
                            <li>
                                <a href="http://www.youtube.com/user/AIMSscicomm">
                                    <img width="24" height="24" alt="Youtube"
                                         src="http://www.aims.gov.au/aims-theme/images/youtube.png"/>
                                </a>
                            </li>
                            <li>
                                <a href="http://twitter.com/aims_gov_au">
                                    <img width="24" height="24" alt="Twitter"
                                         src="http://www.aims.gov.au/aims-theme/images/twitter.png"/>
                                </a>
                            </li>
                            <li>
                                <a href="http://www.facebook.com/pages/Australian-Institute-of-Marine-Science/147289085300030">
                                    <img width="24" height="24" alt="Facebook"
                                         src="http://www.aims.gov.au/aims-theme/images/facebook.png"/>
                                </a>
                            </li>
                            <li>
                                <a href="http://www.flickr.com/photos/aims_gov_au/">
                                    <img width="24" height="24" alt="Flickr"
                                         src="http://www.aims.gov.au/aims-theme/images/flickr.png"/>
                                </a>
                            </li>
                        </ul>
                        <ul>
                            <li><a href="/webmail">Staff Web Mail</a>|
                            </li>
                            <li>
                                <a href="/docs/about/contacts.html">Contacts</a>
                            </li>
                        </ul>
                    </div>
                    <div style="clear:both;"></div>
                </div>
                <div class="footer-floor">
                    <a href="http://www.aims.gov.au/docs/publications/foi-01-statement.html">
                        <img src="http://www.aims.gov.au/aims-theme/images/FOI.png"/>
                    </a>
                    <a href="http://www.aims.gov.au/ips">
                        <img src="http://www.aims.gov.au/aims-theme/images/IPS.png"/>
                    </a>
                </div>
            </footer>
    </xsl:template>

</xsl:stylesheet>
