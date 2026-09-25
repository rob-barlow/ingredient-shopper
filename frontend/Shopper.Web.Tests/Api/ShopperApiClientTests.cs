using System.Net;
using System.Text;
using Shopper.Web.Api;

namespace Shopper.Web.Tests.Api;

/// <summary>
/// Proves the client generated from the contract (ADR-014) compiles and reads responses correctly.
/// A fake HttpMessageHandler stands in for the backend, so there's no network.
/// </summary>
public class ShopperApiClientTests
{
    private sealed class FakeHandler(HttpStatusCode status, string json) : HttpMessageHandler
    {
        public HttpRequestMessage? LastRequest { get; private set; }

        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken ct)
        {
            LastRequest = request;
            return Task.FromResult(new HttpResponseMessage(status)
            {
                Content = new StringContent(json, Encoding.UTF8, "application/json"),
            });
        }
    }

    private static (ShopperApiClient client, FakeHandler handler) ClientReturning(HttpStatusCode status, string json)
    {
        var handler = new FakeHandler(status, json);
        var http = new HttpClient(handler) { BaseAddress = new Uri("http://backend.test/") };
        return (new ShopperApiClient(http), handler);
    }

    [Fact]
    public async Task GetHealth_CallsTheContractPath_AndReadsTheStatusEnum()
    {
        var (client, handler) = ClientReturning(HttpStatusCode.OK,
            """{"status":"UP","groups":["liveness","readiness"]}""");

        Health health = await client.GetHealthAsync();

        Assert.Equal(HealthStatus.UP, health.Status);
        Assert.Equal("http://backend.test/actuator/health", handler.LastRequest!.RequestUri!.ToString());
    }

    [Fact]
    public async Task GetHealth_503IsADocumentedResponse_SoItThrowsATypedApiException()
    {
        var (client, _) = ClientReturning(HttpStatusCode.ServiceUnavailable, """{"status":"DOWN"}""");

        var ex = await Assert.ThrowsAsync<ApiException<Health>>(() => client.GetHealthAsync());

        Assert.Equal(503, ex.StatusCode);
        Assert.Equal(HealthStatus.DOWN, ex.Result.Status);
    }
}
