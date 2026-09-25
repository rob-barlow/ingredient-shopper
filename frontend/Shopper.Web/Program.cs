using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using Shopper.Web;
using Shopper.Web.Api;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");

// Where the backend is (plan §9). Read from wwwroot/appsettings.json, so deployments change
// config, not code (Article V). Fails fast if missing, rather than calling the wrong server.
var apiBaseUrl = builder.Configuration["ApiBaseUrl"]
    ?? throw new InvalidOperationException("ApiBaseUrl is not set in wwwroot/appsettings.json.");

// The generated client (ADR-014). Components depend on IShopperApiClient, so tests can swap in a fake.
builder.Services.AddScoped<IShopperApiClient>(_ =>
    new ShopperApiClient(new HttpClient { BaseAddress = new Uri(apiBaseUrl) }));

await builder.Build().RunAsync();
