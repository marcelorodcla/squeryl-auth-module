
# Lift SquerylAuth Module

Authentication and Authorization module for Lift-Squeryl.

SquerylAUth is a port of Tim Nelson's [MongoAuth](https://github.com/eltimn/lift-mongoauth) module and Torsten Uhlmann's [MapperAuth](https://github.com/liftmodules/mapperauth) to Squeryl.

# Installation

git clone https://github.com/gensosrl/squeryl-auth-module.git

cd squeryl-auth-module

sbt
> + publishLocal

SBT 0.13

For *Lift 2.6.x* (Scala 2.9 and 2.10 and scala 2.11):

    libraryDependencies += "net.liftmodules" %% "squerylauth_2.6" % "0.3" % "compile",



# Configuration

You must set the __SquerylAuth.authUserMeta__ object that you will be using (see below). Most likely in boot:

    // init mapperauth
    SquerylAuth.authUserMeta.default.set(User)
    SquerylAuth.siteName.default.set("My app")

See _SquerylAuth_ for other settings that can be overriden.

# Creating a User Data Model

This module provides several traits for constructing user model classes, which include roles and permissions.

There are several ways you can utilize this module:

## SimpleUser

_model.SimpleUser_ is a fully implemented user model, but is not extensible in any way. This is only useful for testing and demos.
This shows what is necessary to create a user from _ProtoAuthUser_.

## ProtoAuthUser

_ProtoAuthUser_ and _ProtoAuthUserMeta_ are a pair of traits that can be used to build a user model class and meta object.
_ProtoAuthUser_ has some standard fields. You can add
fields to it, you can modify(override) the ones provided. This is a good place to start. If you find you need to modify the provided fields, you can copy and paste them into your user class and use _SquerylAuthUser_.

## SquerylAuthUser

_SquerylAuthUser_ is a trait for defining a Squeryl/Record class of _AuthUser_ (provides authorization functionality).
This can be used to build a user class from scratch. It only requires id and email fields.

## ProtoAuthUserMeta

_ProtoAuthUserMeta_ is a combination of _AuthUserMeta_ and _UserLifeCycle_ traits. These provide authorization
functionality and login/logout functionality for MetaRecord objects. No matter which version you use for the
Record user class, you can use this trait to define your MetaRecord, if it provides sufficient functionality.

"Remember Me" functionality is provided by _ExtSession_.

_LoginToken_ provides a way for users that forgot their password to log in and change it. Users are sent a link with a token (an UUID)
on the url. When they click on it they can be handled appropriately. The implementation is left up to you.

# Roles and Permissions

Permissions are stored in its own table, "permissions". To access them use APermission, a simple case class. They have three parts; domain, actions, entities. This was heavily
influenced by [Apache Shiro's](http://shiro.apache.org/) WildcardPermission.
Please see the [JavaDoc for WildcardPermission](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/authz/permission/WildcardPermission.html)
for detailed information.

You can either attach permissions directly to a user or create roles with permissions attached and then add these roles to the user.

Example:

    Permission.save(Permission.createUserPermission(user, APermission("printer", "print")))
    Permission.save(Permission.createUserPermission(user, APermission("user", "edit", "123")))

    assert(User.hasPermission(APermission("printer", "manage")) == false)

Role is a Record instance that provides a way to group a set of permissions. A user's full set of permissions is calculated using the permissions from any roles assigned to them and the individual permissions assigned to them. There are also LocParams as well as the User-Meta-Singleton that can be used to check for roles.

Example:

    val superuser = Role.save(Role.findOrCreate("superuser", "a category", Permission.fromAPermission(APermission.all)))

    User.save(user.userRoles.addRole("superuser"))

    assert(User.hasRole("superuser")) == true)
    assert(User.lacksRole("superuser")) == false)
    assert(User.lacksRole("admin")) == true)


# SiteMap LocParams

The _Locs_ trait and companion object provide some useful _LocParams_ that use can use when defing your _SiteMap_.

This code was inspired by the [lift-shiro](https://github.com/timperrett/lift-shiro) module.

Examples:

    Meun.i("Settings") / "settings" >> RequireLoggedIn
    Meun.i("Password") / "password" >> RequireAuthentication
    Meun.i("Admin") / "admin" >> HasRole("admin")
    Meun.i("EditEntitiy") / "admin" / "entity" >> HasPermission(APermission("entity", "edit"))


"Authenticated" means the user logged in by supplying their password. "Logged In" means the user was logged in by either
an ExtSession or LoginToken, or they are Authenticated.

# Localization

A default localization is provided and can be found [here](https://github.com/gensosrl/squeryl-auth-module/blob/master/src/main/resources/toserve/squerylauth.resources.html). If you require another language or would prefer different text, copy the default and subtitute your values. See the [Localization](https://www.assembla.com/spaces/liftweb/wiki/Localization) page on the Liftweb wiki for more information.


# Example Implementation

The [lift-squeryl.g8](https://github.com/gensosrl/lift-squeryl.g8) giter8 template provides a fully functioning implementation of a basic user system.

# Credits

_SquerylAuth_ as well as _lift-squeryl.g8_ are ported from Tim Nelson's [lift-mongoauth](https://github.com/eltimn/lift-mongoauth) and [lift-mongo](https://github.com/eltimn/lift-mongo.g8)  and Torsten Uhlmann's [MapperAuth](https://github.com/liftmodules/mapperauth).

# License

Apache v2.0. See LICENSE.txt
