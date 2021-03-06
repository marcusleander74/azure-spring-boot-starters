## Usage

### Add the dependency

`azure-storage-spring-boot-starter` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-storage-spring-boot-starter</artifactId>
    <version>0.1.7</version>
</dependency>
```

### Add the property setting

Open `application.properties` file and add below property with your Azure Storage connection string.

```
azure.storage.connection-string=DefaultEndpointsProtocol=[http|https];AccountName=myAccountName;AccountKey=myAccountKey
```

### Add auto-wiring code

Add below alike code to auto-wire the `CloudStorageAccount` object. Then you can use it to create the client, such as `CloudBlobClient`. For details usage, please reference this [document](https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-blob-storage).

```
@Autowired
private CloudStorageAccount storageAccount;
```

