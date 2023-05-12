FP-FORDEL
===============
[![Bygg og deploy](https://github.com/navikt/fpfordel/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/fpfordel/actions/workflows/build.yml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_fpfordel)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=coverage)](https://sonarcloud.io/summary/new_code?id=navikt_fpfordel)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=navikt_fpfordel)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_fpfordel)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=navikt_fpfordel)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_fpfordel&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_fpfordel)

Dette er kildkode som dekker applikasjonen for fordeling av søknader fra Selvbetjening mellom Gosys, Infotrygd og FPSAK/FPTIBAKE.
Løsningen prøver å journalføre andre dokumenter også - om det ikke er mulig blir de sendt til manuell journalføring.

### Struktur

Dette er løsning for fordeling av søknader (og inntektsmeldinger).

### Kontekst
#### Automatisk dokument behandling
<!-- PlantUml generated from docs/journalføring-diagram.puml. Regenerate Encoded form on https://planttext.com -->
![journalføring-diagram](docs/journalføring-diagram-Automatisk_journalføring.png)
#### Manuell dokument behandling
![manuell_journalføring-diagram-Manuell_journalføring.png](docs%2Fmanuell_journalf%C3%B8ring-diagram-Manuell_journalføring.png)

### Utviklingshåndbok

[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

### Sikkerhet

Det er mulig å kalle tjenesten med bruk av følgende tokens

- Azure CC
- Azure OBO med følgende rettigheter:
    - fpsak-saksbehandler - manuell journalføring
    - fpsak-veileder
    - fpsak-drift
- TokenX
- STS (fases ut)
- SAML (fases ut)

### Docker

```bash
mvn -B -Dfile.encoding=UTF-8 -DskipTests clean install

docker build -t fpfordel .  
```
