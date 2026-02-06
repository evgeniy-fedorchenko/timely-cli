package com.efedorchenko.timely;

import picocli.CommandLine;


/**
 * Отдает номер версии приложения для picocli. Он просит версию прямо в аннотации,
 * там из пропертей еще рано доставать. Поэтому используется этот провайдер для версии
 */
public final class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[]{AppProperties.version()};
    }
}
