# 構成図

TODO


# 環境構築

## RH-SSOのインストール

[RH-SSO 7.3.0][rhsso730]をダウンロードし、任意の場所に展開する。
既存のインストレーションを用いても構わない。

本番環境の場合は、[パッチ][rhsso733-patch]も適用すること。

## EAPとアダプターのインストール

[EAP 7.2.0][eap720]をダウンロードし、任意の場所に展開する。
既存のインストレーションを用いても構わない。

次に、[EAP 7用のRH-SSOアダプター][rhsso730adapter]をダウンロードし、
上記EAPのインストレーションに被せるように展開する。
これにより、`modules/system/add-ons/keycloak`配下にRH-SSOのアダプターが、
また`bin`配下に設定用のCLIファイルが置かれる。

本番環境の場合は、[EAPのパッチ][eap723-patch]および
[アダプターのパッチ][rhsso733adapter-patch]も適用すること。

## 環境変数とヘルパー関数の設定

`helper.sh`に各サーバーの起動やCLIによるアクセス、アプリケーションのビルド等を
簡単に呼び出せるようにするための関数を定義している。
そこで`SSO_HOME`と`EAP_HOME`の環境変数を使うため、
各サーバーのインストレーションを指すように設定し、
`helper.sh`を読み込ませる。

```shell
export SSO_HOME=/path/to/rh-sso-7.3
export EAP_HOME=/path/to/jboss-eap-7.2
. helper.sh
```

## 各サーバーの起動確認と初期設定

各サーバーは本ディレクトリ内の`standalone.*`を使うように起動され、
インストレーション配下に書き込みは行わない。

RH-SSOのサーバーに対しては管理ユーザを登録し今後の設定作業に用いる。

EAPのサーバーに対してはRH-SSOが用意したCLIファイルによる設定を行う。
これによりstandalone.xmlにkeycloakサブシステムが追加され、
Java EE標準のFORM認証やBASIC認証だけでなくKEYCLOAK認証という種類が選択・使用可能になる。

### standalone.idb

```shell
$ idb-server
(RH-SSOの管理コンソールにアクセスし管理ユーザを登録する)
```

※`idb-server`は`helper.sh`内に登録されている関数で、以下同様。

| idbの管理コンソール         | http://localhost:8180/auth/ |
| idbの管理ユーザ             | admin                       |
| idbの管理ユーザのパスワード | RedHat1!                    |

### standalone.idp

```shell
$ idp-server
(RH-SSOの管理コンソールにアクセスし管理ユーザを登録する)
```

| idpの管理コンソール         | http://localhost:8380/auth/ |
| idpの管理ユーザ             | admin                       |
| idpの管理ユーザのパスワード | RedHat1!                    |

※RH-SSOの管理コンソールには同一ブラウザからは複数同時にログインできないので、
可能であれば別のブラウザからアクセスするといい。

### standalone.app

```shell
$ app-server &
$ app-cli --file=$EAP_HOME/bin/adapter-install.cli
$ app-cli :reload
```

### standalone.svc

```shell
$ svc-server &
$ svc-cli --file=$EAP_HOME/bin/adapter-install.cli
$ svc-cli :reload
```

## アプリケーションのビルドとデプロイの確認

[app-jee-jsp][app-jee-jsp]と[service-jee-jaxrs][service-jee-jaxrs]は
いずれもクイックスタートにあるプロジェクトで、対になって動作する。
すなわち、後者がRESTエンドポイントを用意し、前者はそれを呼び出すUIを提供する。

ルートディレクトリにある[pom.xml][parent-pom.xml]には
Red HatのMavenリポジトリやRH-SSOのBOMの設定が行われ、
両プロジェクトから参照される。

これらはweb.xmlでKEYCLOAK認証を用いるよう設定されているので、
上記のEAPに対する設定が済んでいないとデプロイできない。

```shell
$ deploy-app
(app-jee-jspをビルドしapp-jsp.warを生成、standalone.appにデプロイする)
$ deploy-svc
(service-jee-jaxrsをビルドしservice.warを生成、standalone.svcにデプロイする)
```

## 各サーバーの再起動

各サーバーはCtrl-C等の通常のシグナルによる停止でも問題ないが、
CLIのshutdownコマンドも使うことができる。

```shell
$ idb-cli shutdown
$ idp-cli shutdown
$ app-cli shutdown
$ svc-cli shutdown
$ idb-server &
$ idp-server &
$ app-server &
$ svc-server &
```

必要に応じて`&`を付けてバックグラウンドで起動したり、別ターミナルで起動したりすること。


# リンク集

[rhsso730]: https://access.redhat.com/jbossnetwork/restricted/softwareDetail.html?softwareId=64611&product=core.service.rhsso&version=7.3&downloadType=distributions "rh-sso-7.3.0.GA.zip"

[rhsso730adapter]: https://access.redhat.com/jbossnetwork/restricted/softwareDetail.html?softwareId=64591&product=core.service.rhsso&version=7.3&downloadType=distributions "rh-sso-7.3.0.GA-eap7-adapter.zip"

[rhsso733-patch]: https://access.redhat.com/jbossnetwork/restricted/softwareDetail.html?softwareId=72551&product=core.service.rhsso&version=7.3&downloadType=patches "rh-sso-7.3.3-patch.zip"

[rhsso733adapter-patch]: https://access.redhat.com/jbossnetwork/restricted/softwareDetail.html?softwareId=72531&product=core.service.rhsso&version=7.3&downloadType=patches "rh-sso-7.3.3-eap7-adapter.zip"

[eap720]: https://access.redhat.com/jbossnetwork/restricted/softwareDetail.html?softwareId=64311&product=appplatform&version=7.2&downloadType=distributions "jboss-eap-7.2.0.zip"

[eap723-patch]: https://access.redhat.com/jbossnetwork/restricted/softwareDetail.html?softwareId=71721&product=appplatform&version=7.2&downloadType=patches "jboss-eap-7.2.3-patch.zip"

[parent-pom.xml]: https://github.com/redhat-developer/redhat-sso-quickstarts/blob/7.3.x/pom.xml

[app-jee-jsp]: https://github.com/redhat-developer/redhat-sso-quickstarts/tree/7.3.x/app-jee-jsp

[service-jee-jaxrs]: https://github.com/redhat-developer/redhat-sso-quickstarts/tree/7.3.x/service-jee-jaxrs
