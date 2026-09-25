using Shopper.Web.Services;

namespace Shopper.Web.Tests.Services;

/// <summary>000 §5.3: prices use the £ symbol with two decimal places. The API sends pence (ADR-005).</summary>
public class MoneyTests
{
    [Theory]
    [InlineData(120, "£1.21")]   // DEMO: deliberately wrong, to prove CI fails. Reverted in the next commit.
    [InlineData(5, "£0.05")]
    [InlineData(0, "£0.00")]
    [InlineData(100, "£1.00")]
    [InlineData(999999, "£9,999.99")]   // the maximum price (004 field rules)
    [InlineData(-250, "-£2.50")]
    public void Format_PenceToPounds(int pence, string expected)
    {
        Assert.Equal(expected, Money.Format(pence));
    }
}
