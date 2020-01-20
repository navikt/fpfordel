[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=code_smells)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=coverage)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=security_rating)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)

FPFORDEL
===============



Dette er kildkode som dekker applikasjonen for fordeling av søknader fra Selvbetjening mellom Gosys, Infotrygd og FPSAK

### Struktur
Dette er løsning for fordeling av søknader (og inntektsmeldinger).

### Utviklingshåndbok
[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)


### Docker

```bash
mvn -B -Dfile.encoding=UTF-8 -DskipTests clean install

docker build -t fpfordel .  
```
