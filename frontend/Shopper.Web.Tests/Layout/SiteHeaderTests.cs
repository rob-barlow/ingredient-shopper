using Bunit;
using Shopper.Web.Layout;

namespace Shopper.Web.Tests.Layout;

/// <summary>
/// bUnit renders a component in memory, with no browser, and lets you inspect its HTML.
/// </summary>
public class SiteHeaderTests : BunitContext
{
    [Fact]
    public void Header_ShowsStoreNameLinkingHome()
    {
        var cut = Render<SiteHeader>();   // "cut" = component under test

        var brand = cut.Find("a.brand");
        Assert.Equal("Glenda's Groceries", brand.TextContent);
        Assert.Equal("/", brand.GetAttribute("href"));
    }
}
